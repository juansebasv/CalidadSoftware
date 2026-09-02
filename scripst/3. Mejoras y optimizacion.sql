-- ============================================================================
--  MIGRACION - Mejoras y optimizacion sobre una BD que ya tiene el esquema
--  original (scripst/1. Creacion de tablas base.sql).
--
--  Para instalaciones NUEVAS no hace falta: docker/initdb/01-schema.sql ya
--  crea el esquema optimizado. Este script lleva una BD vieja al mismo estado.
--
--  Ejecutar como:  psql -U postgres -d Sistema_Notas -f "3. Mejoras y optimizacion.sql"
-- ============================================================================

BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION fn_set_fecha_modificacion() RETURNS trigger AS $$
BEGIN NEW.fecha_modificacion = now(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------------
--  USUARIO
-- ---------------------------------------------------------------------------
ALTER TABLE usuario
    ALTER COLUMN clave TYPE varchar(120),
    ALTER COLUMN nombre TYPE varchar(80),
    ALTER COLUMN tipo SET DEFAULT 'CONSULTA',
    ADD COLUMN IF NOT EXISTS activo             boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS intentos_fallidos  smallint    NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS bloqueado_hasta    timestamptz,
    ADD COLUMN IF NOT EXISTS ultimo_acceso      timestamptz,
    ADD COLUMN IF NOT EXISTS fecha_creacion     timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS fecha_modificacion timestamptz;

UPDATE usuario SET tipo = 'CONSULTA' WHERE tipo IS NULL OR tipo NOT IN ('ADMIN','COORDINADOR','PROFESOR','CONSULTA');
ALTER TABLE usuario ALTER COLUMN tipo SET NOT NULL;

ALTER TABLE usuario
    ADD CONSTRAINT uq_usuario_login    UNIQUE (login),
    ADD CONSTRAINT ck_usuario_tipo     CHECK (tipo IN ('ADMIN','COORDINADOR','PROFESOR','CONSULTA')),
    ADD CONSTRAINT ck_usuario_intentos CHECK (intentos_fallidos >= 0);

CREATE INDEX IF NOT EXISTS ix_usuario_tipo        ON usuario(tipo);
CREATE INDEX IF NOT EXISTS ix_usuario_activo      ON usuario(activo);
CREATE INDEX IF NOT EXISTS ix_usuario_login_lower ON usuario(lower(login));
DROP TRIGGER IF EXISTS tg_usuario_mod ON usuario;
CREATE TRIGGER tg_usuario_mod BEFORE UPDATE ON usuario
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ---------------------------------------------------------------------------
--  MATERIA   (varchar(1) -> smallint en creditos / ih / semestre)
-- ---------------------------------------------------------------------------
ALTER TABLE materia
    ALTER COLUMN nombre TYPE varchar(120),
    ALTER COLUMN codigo TYPE varchar(20),
    ALTER COLUMN creditos TYPE smallint USING creditos::smallint,
    ALTER COLUMN ih       TYPE smallint USING ih::smallint,
    ALTER COLUMN semestre TYPE smallint USING semestre::smallint,
    ADD COLUMN IF NOT EXISTS activa             boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS fecha_creacion     timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS fecha_modificacion timestamptz;

ALTER TABLE materia
    ADD CONSTRAINT uq_materia_codigo   UNIQUE (codigo),
    ADD CONSTRAINT ck_materia_tipo     CHECK (tipo IN ('T','P','TP')),
    ADD CONSTRAINT ck_materia_creditos CHECK (creditos BETWEEN 1 AND 12),
    ADD CONSTRAINT ck_materia_ih       CHECK (ih BETWEEN 1 AND 20),
    ADD CONSTRAINT ck_materia_semestre CHECK (semestre BETWEEN 1 AND 12);

CREATE INDEX IF NOT EXISTS ix_materia_semestre ON materia(semestre);
CREATE INDEX IF NOT EXISTS ix_materia_tipo     ON materia(tipo);
CREATE INDEX IF NOT EXISTS ix_materia_activa   ON materia(activa);
DROP TRIGGER IF EXISTS tg_materia_mod ON materia;
CREATE TRIGGER tg_materia_mod BEFORE UPDATE ON materia
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ---------------------------------------------------------------------------
--  PROFESOR   (+ enlace a usuario)
-- ---------------------------------------------------------------------------
ALTER TABLE profesor
    ALTER COLUMN nombre TYPE varchar(80),
    ADD COLUMN IF NOT EXISTS id_usuario         integer,
    ADD COLUMN IF NOT EXISTS activo             boolean     NOT NULL DEFAULT true,
    ADD COLUMN IF NOT EXISTS fecha_creacion     timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS fecha_modificacion timestamptz;

ALTER TABLE profesor
    ADD CONSTRAINT uq_profesor_codigo   UNIQUE (codigo),
    ADD CONSTRAINT uq_profesor_usuario  UNIQUE (id_usuario),
    ADD CONSTRAINT fk_profesor_usuario  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    ADD CONSTRAINT ck_profesor_contrato CHECK (tipo_contrato IN ('PLANTA','CATEDRA','OCASIONAL')),
    ADD CONSTRAINT ck_profesor_disp     CHECK (disponibilidad IN ('0','1'));

CREATE INDEX IF NOT EXISTS ix_profesor_contrato ON profesor(tipo_contrato);
CREATE INDEX IF NOT EXISTS ix_profesor_activo   ON profesor(activo);
CREATE INDEX IF NOT EXISTS ix_profesor_usuario  ON profesor(id_usuario);
DROP TRIGGER IF EXISTS tg_profesor_mod ON profesor;
CREATE TRIGGER tg_profesor_mod BEFORE UPDATE ON profesor
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ---------------------------------------------------------------------------
--  PERFIL
-- ---------------------------------------------------------------------------
ALTER TABLE perfil
    ALTER COLUMN id_profesor SET NOT NULL,
    ALTER COLUMN id_materia  SET NOT NULL,
    ADD COLUMN IF NOT EXISTS fecha_creacion timestamptz NOT NULL DEFAULT now(),
    ADD CONSTRAINT uq_perfil_profesor_materia UNIQUE (id_profesor, id_materia);
CREATE INDEX IF NOT EXISTS ix_perfil_profesor ON perfil(id_profesor);
CREATE INDEX IF NOT EXISTS ix_perfil_materia  ON perfil(id_materia);

-- ---------------------------------------------------------------------------
--  HORARIO_PROFESOR
-- ---------------------------------------------------------------------------
ALTER TABLE horario_profesor
    ALTER COLUMN id_profesor SET NOT NULL,
    ALTER COLUMN dia    SET NOT NULL,
    ALTER COLUMN hora   SET NOT NULL,
    ALTER COLUMN estado SET NOT NULL,
    ALTER COLUMN manual SET NOT NULL,
    ADD COLUMN IF NOT EXISTS fecha_creacion     timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS fecha_modificacion timestamptz,
    ADD CONSTRAINT ck_hp_dia    CHECK (dia IN ('1','2','3','4','5','6','7')),
    ADD CONSTRAINT ck_hp_estado CHECK (estado IN ('0','1')),
    ADD CONSTRAINT ck_hp_manual CHECK (manual IN ('0','1'));
CREATE INDEX IF NOT EXISTS ix_hp_profesor        ON horario_profesor(id_profesor);
CREATE INDEX IF NOT EXISTS ix_hp_prof_dia_estado ON horario_profesor(id_profesor, dia, estado);
CREATE INDEX IF NOT EXISTS ix_hp_dia_hora        ON horario_profesor(dia, hora);
DROP TRIGGER IF EXISTS tg_hp_mod ON horario_profesor;
CREATE TRIGGER tg_hp_mod BEFORE UPDATE ON horario_profesor
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ---------------------------------------------------------------------------
--  HORARIO_MATERIA   (+ docente asignado)
-- ---------------------------------------------------------------------------
ALTER TABLE horario_materia
    ALTER COLUMN id_materia SET NOT NULL,
    ALTER COLUMN grupo    SET NOT NULL,
    ALTER COLUMN dia      SET NOT NULL,
    ALTER COLUMN duracion TYPE smallint USING duracion::smallint,
    ALTER COLUMN semestre TYPE smallint USING semestre::smallint,
    ADD COLUMN IF NOT EXISTS id_profesor        integer,
    ADD COLUMN IF NOT EXISTS fecha_creacion     timestamptz NOT NULL DEFAULT now(),
    ADD COLUMN IF NOT EXISTS fecha_modificacion timestamptz,
    ADD CONSTRAINT fk_hm_profesor FOREIGN KEY (id_profesor) REFERENCES profesor(id_profesor) ON DELETE SET NULL,
    ADD CONSTRAINT ck_hm_dia      CHECK (dia IN ('1','2','3','4','5','6','7')),
    ADD CONSTRAINT ck_hm_duracion CHECK (duracion BETWEEN 1 AND 8),
    ADD CONSTRAINT ck_hm_semestre CHECK (semestre BETWEEN 1 AND 12),
    ADD CONSTRAINT ck_hm_jornada  CHECK (jornada IN ('D','N')),
    ADD CONSTRAINT ck_hm_ventana  CHECK ((hora)::int + duracion <= 23);
CREATE INDEX IF NOT EXISTS ix_hm_materia          ON horario_materia(id_materia);
CREATE INDEX IF NOT EXISTS ix_hm_profesor         ON horario_materia(id_profesor);
CREATE INDEX IF NOT EXISTS ix_hm_sem_jor_dia_hora ON horario_materia(semestre, jornada, dia, hora);
CREATE INDEX IF NOT EXISTS ix_hm_dia_hora         ON horario_materia(dia, hora);
DROP TRIGGER IF EXISTS tg_hm_mod ON horario_materia;
CREATE TRIGGER tg_hm_mod BEFORE UPDATE ON horario_materia
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ---------------------------------------------------------------------------
--  AUDITORIA   (tabla nueva)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria   bigserial   PRIMARY KEY,
    fecha          timestamptz NOT NULL DEFAULT now(),
    usuario_login  varchar(50),
    accion         varchar(20) NOT NULL,
    entidad        varchar(40),
    entidad_id     varchar(40),
    resultado      varchar(10) NOT NULL,
    detalle        text,
    ip             varchar(45),
    correlation_id varchar(36),
    CONSTRAINT ck_auditoria_accion    CHECK (accion IN ('LOGIN','LOGOUT','LOGIN_FALLIDO','CUENTA_BLOQUEADA',
                                                        'CREAR','ACTUALIZAR','ELIMINAR','CONSULTAR')),
    CONSTRAINT ck_auditoria_resultado CHECK (resultado IN ('EXITO','ERROR'))
);
CREATE INDEX IF NOT EXISTS ix_auditoria_fecha   ON auditoria(fecha DESC);
CREATE INDEX IF NOT EXISTS ix_auditoria_usuario ON auditoria(usuario_login, fecha DESC);
CREATE INDEX IF NOT EXISTS ix_auditoria_accion  ON auditoria(accion, fecha DESC);
CREATE INDEX IF NOT EXISTS ix_auditoria_entidad ON auditoria(entidad, entidad_id);

COMMIT;

ANALYZE;

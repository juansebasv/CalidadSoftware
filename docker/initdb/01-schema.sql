-- ============================================================================
--  ESQUEMA - Sistema de Asignacion de Horarios Docentes
--  (proyecto de Calidad de Software)
--
--  Mantiene las 6 tablas y los nombres de columna del script original
--  (scripst/1. Creacion de tablas base.sql) y aplica optimizaciones
--  profesionales: tipos correctos, restricciones CHECK para los enums,
--  claves naturales UNIQUE, columnas de auditoria + trigger, control de
--  bloqueo de cuenta e INDICES en todas las FK y en los patrones de
--  consulta. Se agrega la tabla 'auditoria' y dos columnas de enlace
--  (profesor.id_usuario, horario_materia.id_profesor).
--
--  Este fichero lo ejecuta automaticamente el contenedor de PostgreSQL.
--  Para una BD ya existente, ver scripst/3. Mejoras y optimizacion.sql
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- hashes BCrypt en la semilla

-- ---------------------------------------------------------------------------
--  Funcion + trigger de auditoria de modificacion
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_set_fecha_modificacion() RETURNS trigger AS $$
BEGIN
    NEW.fecha_modificacion = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ===========================================================================
--  USUARIO  -  autenticacion, rol y politica de bloqueo
-- ===========================================================================
CREATE TABLE usuario (
    id_usuario         serial       PRIMARY KEY,
    login              varchar(50)  NOT NULL,
    nombre             varchar(80)  NOT NULL,
    clave              varchar(120) NOT NULL,                 -- BCrypt = 60 chars
    tipo               varchar(15)  NOT NULL DEFAULT 'CONSULTA',
    activo             boolean      NOT NULL DEFAULT true,
    intentos_fallidos  smallint     NOT NULL DEFAULT 0,
    bloqueado_hasta    timestamptz,
    ultimo_acceso      timestamptz,
    fecha_creacion     timestamptz  NOT NULL DEFAULT now(),
    fecha_modificacion timestamptz,
    CONSTRAINT uq_usuario_login     UNIQUE (login),
    CONSTRAINT ck_usuario_tipo      CHECK (tipo IN ('ADMIN','COORDINADOR','PROFESOR','CONSULTA')),
    CONSTRAINT ck_usuario_intentos  CHECK (intentos_fallidos >= 0),
    CONSTRAINT ck_usuario_nombre    CHECK (char_length(btrim(nombre)) >= 3),
    CONSTRAINT ck_usuario_login_fmt CHECK (login ~ '^[A-Za-z0-9._-]{4,50}$')
);
CREATE INDEX ix_usuario_tipo         ON usuario(tipo);
CREATE INDEX ix_usuario_activo       ON usuario(activo);
CREATE INDEX ix_usuario_login_lower  ON usuario(lower(login));
CREATE TRIGGER tg_usuario_mod BEFORE UPDATE ON usuario
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ===========================================================================
--  MATERIA
-- ===========================================================================
CREATE TABLE materia (
    id_materia         serial       PRIMARY KEY,
    codigo             varchar(20)  NOT NULL,
    nombre             varchar(120) NOT NULL,
    tipo               varchar(2)   NOT NULL,                 -- T | P | TP
    creditos           smallint     NOT NULL,
    ih                 smallint     NOT NULL,                 -- intensidad horaria semanal
    semestre           smallint     NOT NULL,
    activa             boolean      NOT NULL DEFAULT true,
    fecha_creacion     timestamptz  NOT NULL DEFAULT now(),
    fecha_modificacion timestamptz,
    CONSTRAINT uq_materia_codigo    UNIQUE (codigo),
    CONSTRAINT ck_materia_tipo      CHECK (tipo IN ('T','P','TP')),
    CONSTRAINT ck_materia_creditos  CHECK (creditos BETWEEN 1 AND 12),
    CONSTRAINT ck_materia_ih        CHECK (ih BETWEEN 1 AND 20),
    CONSTRAINT ck_materia_semestre  CHECK (semestre BETWEEN 1 AND 12),
    CONSTRAINT ck_materia_nombre    CHECK (char_length(btrim(nombre)) >= 3),
    CONSTRAINT ck_materia_codigo_fmt CHECK (codigo ~ '^[A-Z0-9-]{3,20}$')
);
CREATE INDEX ix_materia_semestre ON materia(semestre);
CREATE INDEX ix_materia_tipo     ON materia(tipo);
CREATE INDEX ix_materia_activa   ON materia(activa);
CREATE TRIGGER tg_materia_mod BEFORE UPDATE ON materia
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ===========================================================================
--  PROFESOR  (+ enlace opcional a su cuenta de usuario)
-- ===========================================================================
CREATE TABLE profesor (
    id_profesor        serial       PRIMARY KEY,
    codigo             varchar(12)  NOT NULL,
    nombre             varchar(80)  NOT NULL,
    tipo_contrato      varchar(15)  NOT NULL,
    disponibilidad     varchar(1)   NOT NULL DEFAULT '1',
    id_usuario         integer,
    activo             boolean      NOT NULL DEFAULT true,
    fecha_creacion     timestamptz  NOT NULL DEFAULT now(),
    fecha_modificacion timestamptz,
    CONSTRAINT uq_profesor_codigo   UNIQUE (codigo),
    CONSTRAINT uq_profesor_usuario  UNIQUE (id_usuario),
    CONSTRAINT fk_profesor_usuario  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    CONSTRAINT ck_profesor_contrato CHECK (tipo_contrato IN ('PLANTA','CATEDRA','OCASIONAL')),
    CONSTRAINT ck_profesor_disp     CHECK (disponibilidad IN ('0','1')),
    CONSTRAINT ck_profesor_nombre   CHECK (char_length(btrim(nombre)) >= 3),
    CONSTRAINT ck_profesor_codigo_fmt CHECK (codigo ~ '^[A-Z0-9-]{3,12}$')
);
CREATE INDEX ix_profesor_contrato ON profesor(tipo_contrato);
CREATE INDEX ix_profesor_activo   ON profesor(activo);
CREATE INDEX ix_profesor_usuario  ON profesor(id_usuario);
CREATE TRIGGER tg_profesor_mod BEFORE UPDATE ON profesor
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ===========================================================================
--  PERFIL  -  materias que cada profesor esta habilitado a dictar (M:N)
-- ===========================================================================
CREATE TABLE perfil (
    id_perfil      serial      PRIMARY KEY,
    id_profesor    integer     NOT NULL,
    id_materia     integer     NOT NULL,
    fecha_creacion timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_perfil_profesor FOREIGN KEY (id_profesor) REFERENCES profesor(id_profesor) ON DELETE CASCADE,
    CONSTRAINT fk_perfil_materia  FOREIGN KEY (id_materia)  REFERENCES materia(id_materia)  ON DELETE CASCADE,
    CONSTRAINT uq_perfil_profesor_materia UNIQUE (id_profesor, id_materia)
);
CREATE INDEX ix_perfil_profesor ON perfil(id_profesor);
CREATE INDEX ix_perfil_materia  ON perfil(id_materia);

-- ===========================================================================
--  HORARIO_PROFESOR  -  rejilla de disponibilidad del docente
-- ===========================================================================
CREATE TABLE horario_profesor (
    id_horario_profesor serial      PRIMARY KEY,
    id_profesor         integer     NOT NULL,
    dia                 varchar(1)  NOT NULL,
    hora                varchar(2)  NOT NULL,
    estado              varchar(1)  NOT NULL DEFAULT '0',     -- 0 libre / 1 ocupado
    manual              varchar(1)  NOT NULL DEFAULT '0',     -- 0 generado / 1 manual
    fecha_creacion      timestamptz NOT NULL DEFAULT now(),
    fecha_modificacion  timestamptz,
    CONSTRAINT fk_hp_profesor    FOREIGN KEY (id_profesor) REFERENCES profesor(id_profesor) ON DELETE CASCADE,
    CONSTRAINT uq_hp_prof_dia_hora UNIQUE (id_profesor, dia, hora),
    CONSTRAINT ck_hp_dia    CHECK (dia IN ('1','2','3','4','5','6','7')),
    CONSTRAINT ck_hp_hora   CHECK (hora ~ '^(0[6-9]|1[0-9]|2[0-2])$'),
    CONSTRAINT ck_hp_estado CHECK (estado IN ('0','1')),
    CONSTRAINT ck_hp_manual CHECK (manual IN ('0','1'))
);
CREATE INDEX ix_hp_profesor          ON horario_profesor(id_profesor);
CREATE INDEX ix_hp_prof_dia_estado   ON horario_profesor(id_profesor, dia, estado);
CREATE INDEX ix_hp_dia_hora          ON horario_profesor(dia, hora);
CREATE TRIGGER tg_hp_mod BEFORE UPDATE ON horario_profesor
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ===========================================================================
--  HORARIO_MATERIA  -  sesiones de clase programadas (+ docente asignado)
-- ===========================================================================
CREATE TABLE horario_materia (
    id_horario_materia serial      PRIMARY KEY,
    id_materia         integer     NOT NULL,
    id_profesor        integer,
    grupo              varchar(3)  NOT NULL,
    dia                varchar(1)  NOT NULL,
    hora               varchar(2)  NOT NULL,
    duracion           smallint    NOT NULL,                  -- bloques
    semestre           smallint    NOT NULL,
    jornada            varchar(1)  NOT NULL,                  -- D diurna / N nocturna
    fecha_creacion     timestamptz NOT NULL DEFAULT now(),
    fecha_modificacion timestamptz,
    CONSTRAINT fk_hm_materia  FOREIGN KEY (id_materia)  REFERENCES materia(id_materia)  ON DELETE CASCADE,
    CONSTRAINT fk_hm_profesor FOREIGN KEY (id_profesor) REFERENCES profesor(id_profesor) ON DELETE SET NULL,
    CONSTRAINT uq_hm_materia_grupo_dia UNIQUE (id_materia, grupo, dia),
    CONSTRAINT ck_hm_dia      CHECK (dia IN ('1','2','3','4','5','6','7')),
    CONSTRAINT ck_hm_hora     CHECK (hora ~ '^(0[6-9]|1[0-9]|2[0-2])$'),
    CONSTRAINT ck_hm_duracion CHECK (duracion BETWEEN 1 AND 8),
    CONSTRAINT ck_hm_semestre CHECK (semestre BETWEEN 1 AND 12),
    CONSTRAINT ck_hm_jornada  CHECK (jornada IN ('D','N')),
    CONSTRAINT ck_hm_grupo_fmt CHECK (grupo ~ '^[A-Z0-9]{1,3}$'),
    CONSTRAINT ck_hm_ventana  CHECK ((hora)::int + duracion <= 23)
);
CREATE INDEX ix_hm_materia            ON horario_materia(id_materia);
CREATE INDEX ix_hm_profesor           ON horario_materia(id_profesor);
CREATE INDEX ix_hm_sem_jor_dia_hora   ON horario_materia(semestre, jornada, dia, hora);
CREATE INDEX ix_hm_dia_hora           ON horario_materia(dia, hora);
CREATE TRIGGER tg_hm_mod BEFORE UPDATE ON horario_materia
    FOR EACH ROW EXECUTE PROCEDURE fn_set_fecha_modificacion();

-- ===========================================================================
--  AUDITORIA  -  trazabilidad de seguridad y de cambios
-- ===========================================================================
CREATE TABLE auditoria (
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
CREATE INDEX ix_auditoria_fecha   ON auditoria(fecha DESC);
CREATE INDEX ix_auditoria_usuario ON auditoria(usuario_login, fecha DESC);
CREATE INDEX ix_auditoria_accion  ON auditoria(accion, fecha DESC);
CREATE INDEX ix_auditoria_entidad ON auditoria(entidad, entidad_id);

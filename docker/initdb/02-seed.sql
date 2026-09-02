-- ============================================================================
--  DATOS SEMILLA (volumen amplio) - Sistema de Asignacion de Horarios Docentes
--
--  Cobertura de flujos:
--    * Autenticacion: ADMIN / COORDINADOR / PROFESOR / CONSULTA, cuenta
--      bloqueada, cuentas inactivas, cuenta sin primer acceso.
--    * CRUD masivo: 40 usuarios, 44 materias (10 semestres), 35 profesores,
--      ~140 perfiles.
--    * Disponibilidad docente: rejilla completa 06:00-21:00 L-V (+ sabados
--      para planta)  ->  ~2.600 franjas.
--    * Programacion: ~125 sesiones, varios grupos (A/B/N) y jornadas (D/N),
--      con y sin docente asignado.
--    * Escenarios de conflicto para la pantalla de validacion:
--        - DOCENTE_SOLAPADO (choque exacto y solape parcial)
--        - COHORTE_SOLAPADA (mismo semestre/jornada/grupo)
--    * Auditoria: 30 registros historicos + eventos de seguridad.
--
--  Contrasenas (hash BCrypt via pgcrypto, cost 12):
--    admin / dbadmin           -> Admin*2024
--    lgomez / rdiaz / mvega    -> Coord*2024
--    docenteNN (01..25)        -> Prof*2024
--    consulta / auditor* / ... -> Consulta*2024
--    bloqueado                 -> Prof*2024   (bloqueo temporal activo)
--
--  Idempotente: limpia y recarga en cada ejecucion.
-- ============================================================================

BEGIN;

TRUNCATE TABLE auditoria, horario_materia, horario_profesor, perfil,
               profesor, materia, usuario
    RESTART IDENTITY CASCADE;

-- ---------------------------------------------------------------------------
--  USUARIOS
-- ---------------------------------------------------------------------------
INSERT INTO usuario (id_usuario, login, nombre, clave, tipo, activo,
                     intentos_fallidos, bloqueado_hasta, ultimo_acceso) VALUES
 (1, 'admin',  'Administrador del Sistema', crypt('Admin*2024', gen_salt('bf',12)), 'ADMIN',       true, 0, NULL, now() - interval '1 hour'),
 (2, 'dbadmin','Administrador de Datos',    crypt('Admin*2024', gen_salt('bf',12)), 'ADMIN',       true, 0, NULL, now() - interval '2 days'),
 (3, 'lgomez','Laura Gomez',                crypt('Coord*2024', gen_salt('bf',12)), 'COORDINADOR', true, 0, NULL, now() - interval '5 hours'),
 (4, 'rdiaz', 'Ricardo Diaz',               crypt('Coord*2024', gen_salt('bf',12)), 'COORDINADOR', true, 0, NULL, now() - interval '1 day'),
 (5, 'mvega', 'Marcela Vega',               crypt('Coord*2024', gen_salt('bf',12)), 'COORDINADOR', true, 0, NULL, NULL);

SELECT setval('usuario_id_usuario_seq', 5, true);

-- 25 cuentas de profesor con nombre realista y determinista
INSERT INTO usuario (login, nombre, clave, tipo, activo)
SELECT 'docente' || lpad(g::text, 2, '0'),
       (ARRAY['Ana','Luis','Marta','Carlos','Sofia','Diego','Elena','Jorge','Paula','Ivan',
              'Clara','Hugo','Rosa','Mario','Lucia','Nora','Pablo','Sara','Tomas','Vera',
              'Bruno','Alba','Raul','Nadia','Oscar'])[g]
       || ' ' ||
       (ARRAY['Gomez','Ruiz','Diaz','Mora','Vega','Soto','Luna','Pena','Rios','Cano',
              'Ortiz','Bravo','Nieto','Gil','Rey','Pardo','Vidal','Serra','Marin','Roca',
              'Calvo','Lozano','Ferrer','Ibarra','Prieto'])[g],
       crypt('Prof*2024', gen_salt('bf', 12)),
       'PROFESOR',
       true
FROM generate_series(1, 25) g;

SELECT setval('usuario_id_usuario_seq', 30, true);

-- Cuentas de consulta y cuentas en estados especiales
INSERT INTO usuario (id_usuario, login, nombre, clave, tipo, activo,
                     intentos_fallidos, bloqueado_hasta) VALUES
 (31, 'consulta',  'Usuario de Consulta',   crypt('Consulta*2024', gen_salt('bf',12)), 'CONSULTA', true,  0, NULL),
 (32, 'auditor1',  'Auditor Academico Uno', crypt('Consulta*2024', gen_salt('bf',12)), 'CONSULTA', true,  0, NULL),
 (33, 'auditor2',  'Auditor Academico Dos', crypt('Consulta*2024', gen_salt('bf',12)), 'CONSULTA', true,  0, NULL),
 (34, 'decano',    'Decano de Facultad',    crypt('Consulta*2024', gen_salt('bf',12)), 'CONSULTA', true,  0, NULL),
 (35, 'secretaria','Secretaria Academica',  crypt('Consulta*2024', gen_salt('bf',12)), 'CONSULTA', true,  0, NULL),
 (36, 'invitado',  'Cuenta de Invitado',    crypt('Consulta*2024', gen_salt('bf',12)), 'CONSULTA', false, 0, NULL),
 (37, 'bloqueado', 'Docente Bloqueado',     crypt('Prof*2024',     gen_salt('bf',12)), 'PROFESOR', true,  5, now() + interval '30 minutes'),
 (38, 'inactivo',  'Docente Inactivo',      crypt('Prof*2024',     gen_salt('bf',12)), 'PROFESOR', false, 0, NULL),
 (39, 'retirado',  'Docente Retirado',      crypt('Prof*2024',     gen_salt('bf',12)), 'PROFESOR', false, 2, NULL),
 (40, 'nuevo',     'Docente Sin Ingreso',   crypt('Prof*2024',     gen_salt('bf',12)), 'PROFESOR', true,  0, NULL);

SELECT setval('usuario_id_usuario_seq', 40, true);

-- ---------------------------------------------------------------------------
--  MATERIAS  (44, distribuidas en 10 semestres ; tipo T / P / TP)
-- ---------------------------------------------------------------------------
INSERT INTO materia (id_materia, codigo, nombre, tipo, creditos, ih, semestre) VALUES
 (1,  'MAT101',  'Calculo Diferencial',              'T',  4, 6, 1),
 (2,  'INF101',  'Introduccion a la Programacion',   'TP', 3, 5, 1),
 (3,  'MAT103',  'Algebra y Trigonometria',          'T',  3, 4, 1),
 (4,  'FIS101',  'Fisica I',                         'TP', 4, 6, 1),
 (5,  'HUM101',  'Competencias Comunicativas',       'T',  2, 2, 1),
 (6,  'MAT201',  'Calculo Integral',                 'T',  4, 6, 2),
 (7,  'INF201',  'Programacion Orientada a Objetos', 'TP', 4, 6, 2),
 (8,  'MAT203',  'Matematica Discreta',              'T',  3, 4, 2),
 (9,  'FIS201',  'Fisica II',                        'TP', 4, 6, 2),
 (10, 'HUM201',  'Etica Profesional',                'T',  2, 2, 2),
 (11, 'MAT301',  'Ecuaciones Diferenciales',         'T',  3, 4, 3),
 (12, 'INF301',  'Estructuras de Datos',             'TP', 4, 6, 3),
 (13, 'INF303',  'Bases de Datos',                   'TP', 4, 6, 3),
 (14, 'EST301',  'Probabilidad',                     'T',  3, 4, 3),
 (15, 'INF305',  'Arquitectura de Computadores',     'TP', 3, 5, 3),
 (16, 'INF401',  'Analisis de Algoritmos',           'T',  3, 4, 4),
 (17, 'INF403',  'Sistemas Operativos',              'TP', 4, 6, 4),
 (18, 'INF405',  'Redes de Computadores I',          'TP', 3, 5, 4),
 (19, 'INF407',  'Ingenieria de Software I',         'T',  3, 4, 4),
 (20, 'EST401',  'Estadistica Inferencial',          'T',  3, 4, 4),
 (21, 'INF501',  'Calidad de Software',              'TP', 3, 5, 5),
 (22, 'INF503',  'Pruebas de Software',              'TP', 3, 5, 5),
 (23, 'INF505',  'Ingenieria de Software II',        'T',  3, 4, 5),
 (24, 'INF507',  'Redes de Computadores II',         'TP', 3, 5, 5),
 (25, 'INF509',  'Bases de Datos Avanzadas',         'TP', 3, 5, 5),
 (26, 'INF601',  'Inteligencia Artificial',         'TP', 4, 6, 6),
 (27, 'INF603',  'Compiladores',                     'TP', 4, 6, 6),
 (28, 'INF605',  'Seguridad Informatica',            'T',  3, 4, 6),
 (29, 'INF607',  'Sistemas Distribuidos',            'TP', 3, 5, 6),
 (30, 'INF609',  'Computacion Grafica',              'TP', 3, 5, 6),
 (31, 'INF701',  'Aprendizaje Automatico',           'TP', 4, 6, 7),
 (32, 'INF703',  'Mineria de Datos',                 'TP', 3, 5, 7),
 (33, 'INF705',  'Computacion en la Nube',           'TP', 3, 5, 7),
 (34, 'INF707',  'Gerencia de Proyectos de TI',      'T',  3, 4, 7),
 (35, 'INF801',  'Big Data',                         'TP', 4, 6, 8),
 (36, 'INF803',  'Cultura DevOps',                   'TP', 3, 5, 8),
 (37, 'INF805',  'Arquitectura Empresarial',         'T',  3, 4, 8),
 (38, 'INF807',  'Electiva Profesional I',           'T',  3, 4, 8),
 (39, 'INF901',  'Trabajo de Grado I',               'P',  4, 4, 9),
 (40, 'INF903',  'Electiva Profesional II',          'T',  3, 4, 9),
 (41, 'INF905',  'Emprendimiento de Base Tecnologica','T', 2, 2, 9),
 (42, 'INF1001', 'Trabajo de Grado II',              'P',  6, 6, 10),
 (43, 'INF1003', 'Practica Profesional',             'P',  8, 12, 10),
 (44, 'HUM1001', 'Legislacion y TIC',                'T',  2, 2, 10);

SELECT setval('materia_id_materia_seq', 44, true);

-- ---------------------------------------------------------------------------
--  PROFESORES  (35 ; 1..25 con cuenta de usuario, 26..35 sin cuenta)
--    tipo_contrato ciclico, disponibilidad '0' para 1 de cada 7
-- ---------------------------------------------------------------------------
INSERT INTO profesor (codigo, nombre, tipo_contrato, disponibilidad, id_usuario)
SELECT 'DOC-' || lpad(g::text, 4, '0'),
       COALESCE(
         (SELECT nombre FROM usuario WHERE login = 'docente' || lpad(g::text, 2, '0')),
         (ARRAY['Nelson Ayala','Gloria Bermudez','Ramiro Castro','Diana Espinosa','Fabio Gaviria',
                'Helena Jimenez','Kevin Ladino','Monica Naranjo','Pedro Quintana','Silvia Rojas'])[g - 25]
       ),
       (ARRAY['PLANTA','CATEDRA','OCASIONAL'])[1 + (g % 3)],
       CASE WHEN g % 7 = 0 THEN '0' ELSE '1' END,
       (SELECT id_usuario FROM usuario WHERE login = 'docente' || lpad(g::text, 2, '0'))
FROM generate_series(1, 35) g;

SELECT setval('profesor_id_profesor_seq', 35, true);

-- ---------------------------------------------------------------------------
--  PERFIL  (cada profesor habilitado en 4 materias, deterministico)
-- ---------------------------------------------------------------------------
INSERT INTO perfil (id_profesor, id_materia)
SELECT id_profesor, id_materia FROM (
    SELECT p.id_profesor, m.id_materia,
           row_number() OVER (PARTITION BY p.id_profesor
                              ORDER BY ((p.id_profesor * 17 + m.id_materia * 5) % 23)) AS rn
    FROM profesor p CROSS JOIN materia m
) t
WHERE rn <= 4;

-- ---------------------------------------------------------------------------
--  HORARIO_PROFESOR  (rejilla completa 06:00-21:00 Lun-Vie ; sabados planta)
-- ---------------------------------------------------------------------------
INSERT INTO horario_profesor (id_profesor, dia, hora, estado, manual)
SELECT p.id_profesor, d.dia, lpad(h::text, 2, '0'), '0', '0'
FROM   profesor p
CROSS JOIN (VALUES ('1'),('2'),('3'),('4'),('5')) d(dia)
CROSS JOIN generate_series(6, 21) h
WHERE  p.disponibilidad = '1';

INSERT INTO horario_profesor (id_profesor, dia, hora, estado, manual)
SELECT p.id_profesor, '6', lpad(h::text, 2, '0'), '0', '1'
FROM   profesor p
CROSS JOIN generate_series(8, 12) h
WHERE  p.tipo_contrato = 'PLANTA';

-- ---------------------------------------------------------------------------
--  HORARIO_MATERIA  (sesiones programadas)
-- ---------------------------------------------------------------------------
-- Grupo A, primera sesion semanal de cada materia
INSERT INTO horario_materia (id_materia, grupo, dia, hora, duracion, semestre, jornada)
SELECT m.id_materia, 'A',
       (1 + (m.id_materia % 5))::text,
       lpad((7 + (m.id_materia % 6) * 2)::text, 2, '0'),
       2, m.semestre,
       CASE WHEN m.semestre >= 8 THEN 'N' ELSE 'D' END
FROM materia m;

-- Grupo A, segunda sesion semanal (otro dia)
INSERT INTO horario_materia (id_materia, grupo, dia, hora, duracion, semestre, jornada)
SELECT m.id_materia, 'A',
       (1 + ((m.id_materia + 2) % 5))::text,
       lpad((7 + (m.id_materia % 6) * 2)::text, 2, '0'),
       2, m.semestre,
       CASE WHEN m.semestre >= 8 THEN 'N' ELSE 'D' END
FROM materia m;

-- Grupo B para los primeros 4 semestres
INSERT INTO horario_materia (id_materia, grupo, dia, hora, duracion, semestre, jornada)
SELECT m.id_materia, 'B',
       (1 + ((m.id_materia + 1) % 5))::text,
       lpad((8 + (m.id_materia % 5) * 2)::text, 2, '0'),
       2, m.semestre, 'D'
FROM materia m WHERE m.semestre <= 4;

-- Grupo N (nocturno) para semestres 5 a 7
INSERT INTO horario_materia (id_materia, grupo, dia, hora, duracion, semestre, jornada)
SELECT m.id_materia, 'N',
       (1 + (m.id_materia % 5))::text,
       lpad((18 + (m.id_materia % 2) * 2)::text, 2, '0'),
       2, m.semestre, 'N'
FROM materia m WHERE m.semestre BETWEEN 5 AND 7;

-- Asignacion de docente: profesor habilitado (perfil), activo y disponible.
-- Se deja ~1 de cada 7 sesiones sin docente para ejercitar ese flujo.
UPDATE horario_materia hm
SET id_profesor = (
    SELECT pf.id_profesor
    FROM perfil pf
    JOIN profesor pr ON pr.id_profesor = pf.id_profesor
    WHERE pf.id_materia = hm.id_materia
      AND pr.activo AND pr.disponibilidad = '1'
    ORDER BY (pf.id_profesor * 13 + hm.id_horario_materia * 7) % 101
    LIMIT 1
)
WHERE hm.grupo IN ('A', 'B', 'N')
  AND (hm.id_horario_materia % 7) <> 0;

-- Escenarios de CONFLICTO explicitos ----------------------------------------
-- 1) DOCENTE_SOLAPADO exacto: profesor 1, Lunes 07:00, dos materias
INSERT INTO horario_materia (id_materia, id_profesor, grupo, dia, hora, duracion, semestre, jornada) VALUES
 (2, 1, 'Z', '1', '07', 2, 1, 'D'),
 (4, 1, 'Z', '1', '07', 2, 1, 'D');

-- 2) DOCENTE_SOLAPADO parcial: profesor 3, Jueves 10:00-12:00 vs 11:00-13:00
INSERT INTO horario_materia (id_materia, id_profesor, grupo, dia, hora, duracion, semestre, jornada) VALUES
 (6, 3, 'Z', '4', '10', 2, 2, 'D'),
 (8, 3, 'Z', '4', '11', 2, 2, 'D');

-- 3) COHORTE_SOLAPADA: semestre 5, jornada D, grupo C, Miercoles 09:00
INSERT INTO horario_materia (id_materia, id_profesor, grupo, dia, hora, duracion, semestre, jornada) VALUES
 (21, NULL, 'C', '3', '09', 2, 5, 'D'),
 (22, NULL, 'C', '3', '09', 2, 5, 'D');

-- Sesiones sin docente asignado (flujo "sin docente")
INSERT INTO horario_materia (id_materia, id_profesor, grupo, dia, hora, duracion, semestre, jornada) VALUES
 (13, NULL, 'D', '2', '14', 3, 3, 'D'),
 (16, NULL, 'D', '5', '16', 4, 4, 'D'),
 (31, NULL, 'D', '2', '18', 2, 7, 'N');

-- Marcar como ocupadas las franjas de disponibilidad realmente usadas
UPDATE horario_profesor hp
SET    estado = '1'
FROM   horario_materia hm
WHERE  hp.id_profesor = hm.id_profesor
  AND  hp.dia  = hm.dia
  AND  hp.hora = hm.hora;

-- ---------------------------------------------------------------------------
--  AUDITORIA  (historico + eventos de seguridad)
-- ---------------------------------------------------------------------------
INSERT INTO auditoria (fecha, usuario_login, accion, entidad, entidad_id, resultado, detalle, ip, correlation_id)
SELECT now() - (g || ' hours')::interval,
       (ARRAY['admin','lgomez','rdiaz','docente01','docente05'])[1 + (g % 5)],
       (ARRAY['LOGIN','CREAR','ACTUALIZAR','CONSULTAR','ELIMINAR'])[1 + (g % 5)],
       (ARRAY['MATERIA','PROFESOR','USUARIO','SESION_CLASE','PERFIL'])[1 + (g % 5)],
       (100 + g)::text,
       CASE WHEN g % 9 = 0 THEN 'ERROR' ELSE 'EXITO' END,
       'Evento de auditoria historico numero ' || g,
       '10.0.0.' || (1 + g % 200),
       gen_random_uuid()::text
FROM generate_series(1, 28) g;

INSERT INTO auditoria (fecha, usuario_login, accion, resultado, detalle, ip, correlation_id) VALUES
 (now() - interval '3 hours', 'bloqueado', 'LOGIN_FALLIDO',    'ERROR', 'Credenciales invalidas (intento 5)', '10.0.0.66', gen_random_uuid()::text),
 (now() - interval '3 hours', 'bloqueado', 'CUENTA_BLOQUEADA', 'ERROR', 'Bloqueo temporal por 30 minutos',    '10.0.0.66', gen_random_uuid()::text);

COMMIT;

ANALYZE;

-- ---------------------------------------------------------------------------
--  Resumen de la carga
-- ---------------------------------------------------------------------------
SELECT 'usuario'          AS tabla, count(*) AS filas FROM usuario
UNION ALL SELECT 'materia',          count(*) FROM materia
UNION ALL SELECT 'profesor',         count(*) FROM profesor
UNION ALL SELECT 'perfil',           count(*) FROM perfil
UNION ALL SELECT 'horario_profesor', count(*) FROM horario_profesor
UNION ALL SELECT 'horario_materia',  count(*) FROM horario_materia
UNION ALL SELECT 'auditoria',        count(*) FROM auditoria
ORDER BY tabla;

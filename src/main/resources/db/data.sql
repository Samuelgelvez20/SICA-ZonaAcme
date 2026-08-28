-- ============================================================
-- SICA - Sistema Integrado de Control de Acceso para Zona Acme
-- data.sql: datos semilla para desarrollo y pruebas
--
-- NOTA sobre password_hash: se usa SHA-256 (sin sal, por simplicidad
-- para el alcance del curso) sobre el texto plano "1234" para TODOS
-- los usuarios de ejemplo. El AutenticarUsuarioService (HU-03) debe
-- calcular SHA-256 del password ingresado y compararlo con este valor.
-- ============================================================

INSERT INTO roles (nombre) VALUES ('ADMIN'), ('GUARDA'), ('FUNCIONARIO');

INSERT INTO permisos (codigo, descripcion) VALUES
 ('crear_usuario',      'Crear nuevos usuarios del sistema'),
 ('registrar_visita',   'Registrar una nueva visita (pre-registro o no anunciada)'),
 ('generar_reporte',    'Generar y consultar reportes'),
 ('bloquear_persona',   'Bloquear el acceso de una persona'),
 ('aprobar_visita',     'Aprobar o rechazar una visita pendiente'),
 ('checkin_visita',     'Registrar el ingreso (check-in) de una visita'),
 ('checkout_visita',    'Registrar la salida (check-out) de una visita'),
 ('editar_persona',     'Crear o editar personas y empresas'),
 ('registrar_incidente','Registrar un incidente de seguridad');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT (SELECT id FROM roles WHERE nombre = 'ADMIN'), id FROM permisos;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT (SELECT id FROM roles WHERE nombre = 'GUARDA'), id
FROM permisos
WHERE codigo IN ('registrar_visita', 'checkin_visita', 'checkout_visita', 'editar_persona');

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT (SELECT id FROM roles WHERE nombre = 'FUNCIONARIO'), id
FROM permisos
WHERE codigo IN ('registrar_visita', 'aprobar_visita', 'generar_reporte',
                  'bloquear_persona', 'registrar_incidente', 'editar_persona');

INSERT INTO usuarios (username, password_hash, nombre, rol_id, activo) VALUES
 ('admin',        '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Administrador General', (SELECT id FROM roles WHERE nombre = 'ADMIN'), TRUE),
 ('guarda1',      '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Carlos Pérez',          (SELECT id FROM roles WHERE nombre = 'GUARDA'), TRUE),
 ('funcionario1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'Laura Gómez',           (SELECT id FROM roles WHERE nombre = 'FUNCIONARIO'), TRUE);

INSERT INTO empresas (nombre, nit) VALUES
 ('TechNova S.A.S.', '900123456-1'),
 ('Acme Logistics',  '900654321-2');

INSERT INTO personas (nombre, documento, tipo, foto_url, empresa_id, funcionario_anfitrion_id, bloqueado) VALUES
 ('Juan Torres',      '1001234567', 'TRABAJADOR', 'https://i.pravatar.cc/150?img=12',
    (SELECT id FROM empresas WHERE nombre = 'TechNova S.A.S.'),
    (SELECT id FROM usuarios WHERE username = 'funcionario1'), FALSE),
 ('Maria Rodríguez',  '1009876543', 'INVITADO',   'https://i.pravatar.cc/150?img=32',
    NULL,
    (SELECT id FROM usuarios WHERE username = 'funcionario1'), FALSE);
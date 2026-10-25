-- =====================================================================
-- V3: registro público de usuarios y endurecimiento de la seguridad
--  - nombre_usuario: permite iniciar sesión con usuario o con correo
--  - pendiente_aprobacion: las cuentas creadas por registro esperan
--    a que un administrador las apruebe y les asigne un rol
--  - intentos_fallidos / bloqueado_hasta: bloqueo temporal ante fuerza bruta
-- =====================================================================
ALTER TABLE usuarios ADD COLUMN nombre_usuario VARCHAR(30);
ALTER TABLE usuarios ADD COLUMN pendiente_aprobacion BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuarios ADD COLUMN intentos_fallidos INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuarios ADD COLUMN bloqueado_hasta TIMESTAMP;

UPDATE usuarios SET nombre_usuario = 'admin'    WHERE correo = 'admin@dronemission.local';
UPDATE usuarios SET nombre_usuario = 'operador' WHERE correo = 'operador@dronemission.local';
UPDATE usuarios SET nombre_usuario = 'piloto'   WHERE correo = 'piloto@dronemission.local';
UPDATE usuarios SET nombre_usuario = 'tecnico'  WHERE correo = 'tecnico@dronemission.local';
UPDATE usuarios SET nombre_usuario = 'cliente'  WHERE correo = 'cliente@dronemission.local';
UPDATE usuarios SET nombre_usuario = 'piloto2'  WHERE correo = 'piloto2@dronemission.local';
-- Cualquier otro usuario existente recibe un nombre de usuario derivado de su id
UPDATE usuarios SET nombre_usuario = CONCAT('usuario', CAST(id AS VARCHAR(20))) WHERE nombre_usuario IS NULL;

ALTER TABLE usuarios ALTER COLUMN nombre_usuario SET NOT NULL;
ALTER TABLE usuarios ADD CONSTRAINT uq_usuarios_nombre_usuario UNIQUE (nombre_usuario);
CREATE INDEX ix_usuarios_pendientes ON usuarios(pendiente_aprobacion);

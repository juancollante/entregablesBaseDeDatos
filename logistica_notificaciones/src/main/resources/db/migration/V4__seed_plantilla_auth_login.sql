INSERT INTO plantilla_notificacion (id, codigo, asunto, cuerpo, activo, updated_at)
VALUES
 ('30000000-0000-0000-0000-000000000004','AUTH_LOGIN','Inicio de sesión','Se registró un intento de inicio de sesión en el sistema.',true,now())
ON CONFLICT (codigo) DO NOTHING;

-- Datos iniciales (solo desarrollo). Contraseña: password
-- Hash BCrypt de "password" (cost 10)
INSERT INTO roles (id, codigo, nombre, descripcion, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'ADMIN',
    'Administrador',
    'Acceso administrativo',
    now()
);

INSERT INTO roles (id, codigo, nombre, descripcion, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000002',
    'OPERADOR',
    'Operador de sede',
    'Registro de eventos en sede asignada',
    now()
);

INSERT INTO roles (id, codigo, nombre, descripcion, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000003',
    'CLIENTE',
    'Cliente',
    'Consulta de envíos',
    now()
);

INSERT INTO usuarios (
    id,
    email,
    password_hash,
    nombre,
    apellido,
    rol_id,
    codigo_sede_asignada,
    activo,
    created_at,
    updated_at
) VALUES (
    '10000000-0000-0000-0000-000000000001',
    'admin@logistica.local',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Admin',
    'Sistema',
    '00000000-0000-0000-0000-000000000001',
    null,
    true,
    now(),
    now()
);

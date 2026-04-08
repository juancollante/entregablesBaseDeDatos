-- Usuarios de desarrollo: Operador y Cliente. Contraseña para ambos: password
-- Mismo hash BCrypt que V2 (cost 10)
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
    '10000000-0000-0000-0000-000000000002',
    'operador@logistica.local',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'María',
    'Operadora',
    '00000000-0000-0000-0000-000000000002',
    'SEDE-MDE-01',
    true,
    now(),
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
    '10000000-0000-0000-0000-000000000003',
    'cliente@logistica.local',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Carlos',
    'Cliente',
    '00000000-0000-0000-0000-000000000003',
    null,
    true,
    now(),
    now()
);

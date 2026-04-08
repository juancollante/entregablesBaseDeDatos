-- Esquema BD Autenticación (tablas_bases_de_datos.txt)

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE modulos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(80) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    orden INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE permisos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(160) NOT NULL,
    descripcion VARCHAR(500),
    modulo_id UUID NOT NULL REFERENCES modulos (id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE roles_permisos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rol_id UUID NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permiso_id UUID NOT NULL REFERENCES permisos (id) ON DELETE CASCADE,
    UNIQUE (rol_id, permiso_id)
);

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    apellido VARCHAR(120),
    rol_id UUID NOT NULL REFERENCES roles (id),
    codigo_sede_asignada VARCHAR(50),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE auditoria_autenticacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID REFERENCES usuarios (id),
    tipo_evento VARCHAR(40) NOT NULL,
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    detalle VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE sesiones_activa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    jti VARCHAR(128) NOT NULL UNIQUE,
    ip VARCHAR(64),
    user_agent VARCHAR(512),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    revocada BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE refresh_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revocada BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token (usuario_id);
CREATE INDEX idx_auditoria_usuario ON auditoria_autenticacion (usuario_id);
CREATE INDEX idx_auditoria_created ON auditoria_autenticacion (created_at);

CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    usada_en TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

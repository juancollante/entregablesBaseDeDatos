-- BD Envíos (sin FK a otras bases; coherencia lógica con Seguimiento/Auth vía códigos)

CREATE TABLE estados_envio (
    id         UUID PRIMARY KEY,
    codigo     VARCHAR(50)  NOT NULL UNIQUE,
    nombre     VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500)
);

CREATE TABLE direcciones (
    id                   UUID PRIMARY KEY,
    linea1               VARCHAR(255) NOT NULL,
    linea2               VARCHAR(255),
    municipio_codigo_dane VARCHAR(5),
    municipio_nombre     VARCHAR(120),
    departamento         VARCHAR(80)  NOT NULL DEFAULT 'Antioquia',
    pais                 VARCHAR(80)  NOT NULL DEFAULT 'Colombia',
    codigo_postal        VARCHAR(20),
    referencias          VARCHAR(500)
);

CREATE TABLE remitentes (
    id              UUID PRIMARY KEY,
    nombre_completo VARCHAR(200) NOT NULL,
    documento       VARCHAR(50),
    email           VARCHAR(255),
    telefono        VARCHAR(50),
    direccion_id    UUID NOT NULL REFERENCES direcciones (id)
);

CREATE TABLE destinatarios (
    id              UUID PRIMARY KEY,
    nombre_completo VARCHAR(200) NOT NULL,
    documento       VARCHAR(50),
    email           VARCHAR(255),
    telefono        VARCHAR(50),
    direccion_id    UUID NOT NULL REFERENCES direcciones (id)
);

CREATE TABLE envios (
    id                         UUID PRIMARY KEY,
    numero_guia                VARCHAR(32) NOT NULL UNIQUE,
    estado_envio_id            UUID NOT NULL REFERENCES estados_envio (id),
    remitente_id               UUID NOT NULL REFERENCES remitentes (id),
    destinatario_id            UUID NOT NULL REFERENCES destinatarios (id),
    descripcion_paquete        VARCHAR(500),
    peso_kg                    NUMERIC(10, 2),
    fecha_creacion             TIMESTAMPTZ NOT NULL,
    fecha_estimada_entrega     DATE,
    codigo_sede_registro       VARCHAR(50),
    creado_por_usuario_id_externo UUID,
    updated_at                 TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_envios_estado ON envios (estado_envio_id);
CREATE INDEX idx_envios_fecha_creacion ON envios (fecha_creacion);

CREATE TABLE auditoria_envios (
    id                 UUID PRIMARY KEY,
    envio_id           UUID NOT NULL REFERENCES envios (id),
    accion             VARCHAR(80) NOT NULL,
    usuario_id_externo UUID,
    valores_anteriores JSONB,
    valores_nuevos     JSONB,
    created_at         TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_auditoria_envio ON auditoria_envios (envio_id);

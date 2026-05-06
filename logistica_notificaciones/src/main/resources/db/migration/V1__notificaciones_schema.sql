-- Esquema BD Notificaciones (tablas_bases_de_datos.txt)

CREATE TABLE plantilla_notificacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(120) NOT NULL UNIQUE,
    asunto VARCHAR(300) NOT NULL,
    cuerpo TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    canal VARCHAR(40) NOT NULL,
    destinatario VARCHAR(255) NOT NULL,
    asunto VARCHAR(300),
    cuerpo TEXT,
    plantilla_id UUID REFERENCES plantilla_notificacion (id),
    estado VARCHAR(40) NOT NULL,
    numero_guia VARCHAR(60),
    payload_evento JSONB,
    proveedor_id_mensaje VARCHAR(255),
    error_mensaje VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    sent_at TIMESTAMPTZ
);

CREATE INDEX idx_notif_estado ON notificaciones (estado);
CREATE INDEX idx_notif_guia ON notificaciones (numero_guia);
CREATE INDEX idx_notif_created ON notificaciones (created_at);

CREATE TABLE preferencias_notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id_externo UUID,
    email VARCHAR(255),
    codigo_evento VARCHAR(120) NOT NULL,
    canal VARCHAR(40) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (email, codigo_evento, canal)
);

CREATE TABLE consumo_eventos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key VARCHAR(200) NOT NULL UNIQUE,
    topic VARCHAR(200),
    partition_id INT,
    offset_val BIGINT,
    procesado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE log_auditoria_notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    notificacion_id UUID REFERENCES notificaciones (id) ON DELETE CASCADE,
    nivel VARCHAR(20) NOT NULL,
    mensaje VARCHAR(1000) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


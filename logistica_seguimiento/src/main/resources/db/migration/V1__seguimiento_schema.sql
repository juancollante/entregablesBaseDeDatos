-- Esquema BD Seguimiento (tablas_bases_de_datos.txt)

CREATE TABLE municipios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_dane VARCHAR(5) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE sedes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(200) NOT NULL,
    municipio_id UUID NOT NULL REFERENCES municipios (id),
    direccion VARCHAR(300),
    telefono VARCHAR(80),
    tipo VARCHAR(40) NOT NULL DEFAULT 'OFICINA',
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tipos_evento_seguimiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(80) NOT NULL UNIQUE,
    nombre VARCHAR(160) NOT NULL,
    descripcion VARCHAR(500),
    orden_visual INT,
    activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE eventos_seguimiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_guia VARCHAR(60) NOT NULL,
    tipo_evento_id UUID NOT NULL REFERENCES tipos_evento_seguimiento (id),
    sede_id UUID REFERENCES sedes (id),
    fecha_hora TIMESTAMPTZ NOT NULL,
    descripcion VARCHAR(500),
    metadata JSONB,
    operador_id_externo UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_eventos_guia ON eventos_seguimiento (numero_guia);
CREATE INDEX idx_eventos_fecha ON eventos_seguimiento (fecha_hora);


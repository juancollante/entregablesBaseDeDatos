# Modelo Físico de Base de Datos
## Sistema: Envíos (`logistica-envios`)

## 1. Alcance físico
Este modelo físico está aterrizado para PostgreSQL y define cómo se implementa el esquema en disco y en ejecución:

- Tipos de datos concretos (UUID, TIMESTAMPTZ, JSONB, NUMERIC)
- Claves primarias, foráneas y restricciones CHECK
- Índices de rendimiento para consultas operativas
- Convenciones de nombres para producción
- Script SQL ejecutable por migraciones

## 2. Motor y configuración recomendada

- Motor: PostgreSQL 14+
- Encoding: UTF-8
- Timezone de base de datos: `America/Bogota`
- Extensiones recomendadas:

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- gen_random_uuid()
```

## 3. Esquema físico (DDL)

```sql
-- ==========================================
-- ESQUEMA FISICO - BD ENVIOS
-- ==========================================

CREATE TABLE IF NOT EXISTS estados_envio (
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS direcciones (
    id UUID PRIMARY KEY,
    linea1 VARCHAR(255) NOT NULL,
    linea2 VARCHAR(255),
    municipio_codigo_dane VARCHAR(5),
    municipio_nombre VARCHAR(120),
    departamento VARCHAR(80) NOT NULL DEFAULT 'Antioquia',
    pais VARCHAR(80) NOT NULL DEFAULT 'Colombia',
    codigo_postal VARCHAR(20),
    referencias VARCHAR(500),
    CONSTRAINT ck_direcciones_departamento CHECK (departamento = 'Antioquia'),
    CONSTRAINT ck_direcciones_pais CHECK (pais = 'Colombia')
);

CREATE TABLE IF NOT EXISTS remitentes (
    id UUID PRIMARY KEY,
    nombre_completo VARCHAR(200) NOT NULL,
    documento VARCHAR(50),
    email VARCHAR(255),
    telefono VARCHAR(50),
    direccion_id UUID NOT NULL,
    CONSTRAINT fk_remitentes_direccion
        FOREIGN KEY (direccion_id) REFERENCES direcciones(id),
    CONSTRAINT ck_remitentes_email
        CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')
);

CREATE TABLE IF NOT EXISTS destinatarios (
    id UUID PRIMARY KEY,
    nombre_completo VARCHAR(200) NOT NULL,
    documento VARCHAR(50),
    email VARCHAR(255),
    telefono VARCHAR(50),
    direccion_id UUID NOT NULL,
    CONSTRAINT fk_destinatarios_direccion
        FOREIGN KEY (direccion_id) REFERENCES direcciones(id),
    CONSTRAINT ck_destinatarios_email
        CHECK (email IS NULL OR email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$')
);

CREATE TABLE IF NOT EXISTS envios (
    id UUID PRIMARY KEY,
    numero_guia VARCHAR(32) NOT NULL UNIQUE,
    estado_envio_id UUID NOT NULL,
    remitente_id UUID NOT NULL,
    destinatario_id UUID NOT NULL,
    descripcion_paquete VARCHAR(500),
    peso_kg NUMERIC(10,2),
    fecha_creacion TIMESTAMPTZ NOT NULL,
    fecha_estimada_entrega DATE,
    codigo_sede_registro VARCHAR(50),
    creado_por_usuario_id_externo UUID,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_envios_estado
        FOREIGN KEY (estado_envio_id) REFERENCES estados_envio(id),
    CONSTRAINT fk_envios_remitente
        FOREIGN KEY (remitente_id) REFERENCES remitentes(id),
    CONSTRAINT fk_envios_destinatario
        FOREIGN KEY (destinatario_id) REFERENCES destinatarios(id),

    CONSTRAINT ck_envios_peso_positivo
        CHECK (peso_kg IS NULL OR peso_kg > 0),
    CONSTRAINT ck_envios_fecha_entrega
        CHECK (fecha_estimada_entrega IS NULL OR fecha_estimada_entrega >= fecha_creacion::date)
);

CREATE TABLE IF NOT EXISTS auditoria_envios (
    id UUID PRIMARY KEY,
    envio_id UUID NOT NULL,
    accion VARCHAR(80) NOT NULL,
    usuario_id_externo UUID,
    valores_anteriores JSONB,
    valores_nuevos JSONB,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_auditoria_envio
        FOREIGN KEY (envio_id) REFERENCES envios(id)
);
```

## 4. Índices físicos

```sql
-- Índices existentes y recomendados
CREATE INDEX IF NOT EXISTS idx_envios_estado ON envios (estado_envio_id);
CREATE INDEX IF NOT EXISTS idx_envios_fecha_creacion ON envios (fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_envios_numero_guia ON envios (numero_guia);
CREATE INDEX IF NOT EXISTS idx_envios_estado_fecha ON envios (estado_envio_id, fecha_creacion DESC);

CREATE INDEX IF NOT EXISTS idx_remitentes_documento ON remitentes (documento);
CREATE INDEX IF NOT EXISTS idx_destinatarios_documento ON destinatarios (documento);
CREATE INDEX IF NOT EXISTS idx_direcciones_municipio_dane ON direcciones (municipio_codigo_dane);

CREATE INDEX IF NOT EXISTS idx_auditoria_envio ON auditoria_envios (envio_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_envio_fecha ON auditoria_envios (envio_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_accion_fecha ON auditoria_envios (accion, created_at DESC);
```

## 5. Semillas físicas de catálogo

```sql
INSERT INTO estados_envio (id, codigo, nombre, descripcion) VALUES
    ('a0000001-0000-4000-8000-000000000001', 'CREADO', 'Creado', 'Radicado en el sistema'),
    ('a0000001-0000-4000-8000-000000000002', 'EN_TRANSITO', 'En tránsito', 'En movimiento entre nodos'),
    ('a0000001-0000-4000-8000-000000000003', 'EN_SEDE', 'En sede', 'En oficina o hub'),
    ('a0000001-0000-4000-8000-000000000004', 'ENTREGADO', 'Entregado', 'Entrega confirmada'),
    ('a0000001-0000-4000-8000-000000000005', 'INCIDENCIA', 'Incidencia', 'Requiere atención')
ON CONFLICT (id) DO NOTHING;
```

## 6. Políticas operativas del modelo físico

- `auditoria_envios` es append-only para trazabilidad.
- Los UUID se generan en aplicación o con `gen_random_uuid()`.
- `envios.numero_guia` es clave de negocio única para tracking.
- `municipio_codigo_dane` se valida por API externa (sin FK inter-bd).

## 7. Consultas físicas típicas (optimizadas)

```sql
-- Tracking por guía (usa índice unique)
SELECT *
FROM envios
WHERE numero_guia = 'ECV-2026-0001234';

-- Bandeja operativa por estado y recencia (usa índice compuesto)
SELECT e.id, e.numero_guia, e.fecha_creacion
FROM envios e
WHERE e.estado_envio_id = 'a0000001-0000-4000-8000-000000000002'
ORDER BY e.fecha_creacion DESC
LIMIT 100;

-- Historial de auditoría por envío
SELECT a.accion, a.valores_anteriores, a.valores_nuevos, a.created_at
FROM auditoria_envios a
WHERE a.envio_id = '11111111-1111-4111-8111-111111111111'
ORDER BY a.created_at DESC;
```

## 8. Diferencia con el modelo lógico

- Modelo lógico: qué entidades y relaciones hay.
- Modelo físico: cómo se implementa en PostgreSQL (tipos, índices, constraints, ejecución real).

Este documento representa la capa de implementación real en base de datos para el dominio de envíos.

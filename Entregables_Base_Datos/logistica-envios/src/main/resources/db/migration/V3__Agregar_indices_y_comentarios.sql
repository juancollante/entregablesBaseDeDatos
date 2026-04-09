-- V3__Agregar_indices_y_comentarios.sql
-- Migración: Agregar índices especializados y comentarios a tablas existentes
-- Versión: 3
-- Fecha: 2026-04-07
-- Descripción: Optimiza performance con índices compuestos y añade metadatos en comentarios

-- ==================== ÍNDICES ADICIONALES ====================

-- Estados: búsqueda rápida por código
CREATE INDEX IF NOT EXISTS idx_estados_envio_codigo 
  ON estados_envio (codigo);

-- Direcciones: búsqueda por municipio y DANE
CREATE INDEX IF NOT EXISTS idx_direcciones_municipio 
  ON direcciones (municipio_codigo_dane);

CREATE INDEX IF NOT EXISTS idx_direcciones_municipio_nombre 
  ON direcciones (municipio_nombre);

-- Remitentes: búsqueda por documento y email
CREATE INDEX IF NOT EXISTS idx_remitentes_documento 
  ON remitentes (documento);

CREATE INDEX IF NOT EXISTS idx_remitentes_email 
  ON remitentes (email);

CREATE INDEX IF NOT EXISTS idx_remitentes_direccion 
  ON remitentes (direccion_id);

-- Destinatarios: búsqueda por documento y email
CREATE INDEX IF NOT EXISTS idx_destinatarios_documento 
  ON destinatarios (documento);

CREATE INDEX IF NOT EXISTS idx_destinatarios_email 
  ON destinatarios (email);

CREATE INDEX IF NOT EXISTS idx_destinatarios_direccion 
  ON destinatarios (direccion_id);

-- Envíos: índices compuestos para queries comunes
CREATE INDEX IF NOT EXISTS idx_envios_estado_fecha 
  ON envios (estado_envio_id, fecha_creacion DESC);

CREATE INDEX IF NOT EXISTS idx_envios_remitente 
  ON envios (remitente_id);

CREATE INDEX IF NOT EXISTS idx_envios_destinatario 
  ON envios (destinatario_id);

CREATE INDEX IF NOT EXISTS idx_envios_numero_guia 
  ON envios (numero_guia);

CREATE INDEX IF NOT EXISTS idx_envios_usuario_creador 
  ON envios (creado_por_usuario_id_externo);

CREATE INDEX IF NOT EXISTS idx_envios_sede_registro 
  ON envios (codigo_sede_registro);

-- Auditoría: búsqueda por acción y usuario
CREATE INDEX IF NOT EXISTS idx_auditoria_accion 
  ON auditoria_envios (accion);

CREATE INDEX IF NOT EXISTS idx_auditoria_usuario 
  ON auditoria_envios (usuario_id_externo);

CREATE INDEX IF NOT EXISTS idx_auditoria_fecha 
  ON auditoria_envios (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_auditoria_envio_fecha 
  ON auditoria_envios (envio_id, created_at DESC);

-- ==================== COMENTARIOS EN TABLAS ====================

COMMENT ON TABLE estados_envio IS 'Estados posibles de un envío en el ciclo de vida';

COMMENT ON COLUMN estados_envio.id IS 'Identificador único del estado';
COMMENT ON COLUMN estados_envio.codigo IS 'Código único: CREADO, EN_TRANSITO, EN_SEDE, ENTREGADO, INCIDENCIA';
COMMENT ON COLUMN estados_envio.nombre IS 'Nombre legible del estado para UI';
COMMENT ON COLUMN estados_envio.descripcion IS 'Descripción detallada del estado';

COMMENT ON TABLE direcciones IS 'Direcciones normalizadas para remitentes y destinatarios, alineada a Antioquia (Colombia)';

COMMENT ON COLUMN direcciones.id IS 'Identificador único UUID';
COMMENT ON COLUMN direcciones.linea1 IS 'Primera línea: calle, carrera, dirección principal (requerida)';
COMMENT ON COLUMN direcciones.linea2 IS 'Segunda línea: apartamento, apto_número, complementos';
COMMENT ON COLUMN direcciones.municipio_codigo_dane IS 'Código DANE del municipio para validación (sin FK externa)';
COMMENT ON COLUMN direcciones.municipio_nombre IS 'Nombre del municipio para búsqueda y display';
COMMENT ON COLUMN direcciones.departamento IS 'Departamento fijo: Antioquia (para operación actual)';
COMMENT ON COLUMN direcciones.pais IS 'País fijo: Colombia';
COMMENT ON COLUMN direcciones.codigo_postal IS 'Código postal del municipio';
COMMENT ON COLUMN direcciones.referencias IS 'Referencias adicionales: frente a..., cerca de..., etc.';

COMMENT ON TABLE remitentes IS 'Datos del remitente (quien envía el paquete)';

COMMENT ON COLUMN remitentes.id IS 'Identificador único UUID';
COMMENT ON COLUMN remitentes.nombre_completo IS 'Nombre completo del remitente';
COMMENT ON COLUMN remitentes.documento IS 'Documento de identidad: cédula, pasaporte, etc.';
COMMENT ON COLUMN remitentes.email IS 'Correo para notificaciones';
COMMENT ON COLUMN remitentes.telefono IS 'Número de contacto con código de país/área';
COMMENT ON COLUMN remitentes.direccion_id IS 'FK a direcciones: ubicación del remitente';

COMMENT ON TABLE destinatarios IS 'Datos del destinatario (quien recibe el paquete)';

COMMENT ON COLUMN destinatarios.id IS 'Identificador único UUID';
COMMENT ON COLUMN destinatarios.nombre_completo IS 'Nombre completo del destinatario';
COMMENT ON COLUMN destinatarios.documento IS 'Documento de identidad: cédula, pasaporte, etc.';
COMMENT ON COLUMN destinatarios.email IS 'Correo para notificaciones de entrega';
COMMENT ON COLUMN destinatarios.telefono IS 'Número de contacto para confirmar entrega';
COMMENT ON COLUMN destinatarios.direccion_id IS 'FK a direcciones: ubicación de entrega';

COMMENT ON TABLE envios IS 'Tabla principal: cabecera del envío con guía única, estado, y referencias a remitente/destinatario';

COMMENT ON COLUMN envios.id IS 'Identificador único UUID';
COMMENT ON COLUMN envios.numero_guia IS 'Número de guía único generado al crear envío';
COMMENT ON COLUMN envios.estado_envio_id IS 'FK a estados_envio: estado actual del envío';
COMMENT ON COLUMN envios.remitente_id IS 'FK a remitentes: datos del remitente';
COMMENT ON COLUMN envios.destinatario_id IS 'FK a destinatarios: datos del destinatario';
COMMENT ON COLUMN envios.descripcion_paquete IS 'Descripción del contenido para aduanas/tracking';
COMMENT ON COLUMN envios.peso_kg IS 'Peso total del paquete en kilogramos';
COMMENT ON COLUMN envios.fecha_creacion IS 'Timestamp de creación del envío';
COMMENT ON COLUMN envios.fecha_estimada_entrega IS 'Fecha estimada de entrega al destinatario';
COMMENT ON COLUMN envios.codigo_sede_registro IS 'Código de la sede donde se registró (sin FK externa)';
COMMENT ON COLUMN envios.creado_por_usuario_id_externo IS 'UUID del usuario Auth que creó el envío';
COMMENT ON COLUMN envios.updated_at IS 'Timestamp de última actualización';

COMMENT ON TABLE auditoria_envios IS 'Registro de cambios sensibles en envíos para auditoría y compliance';

COMMENT ON COLUMN auditoria_envios.id IS 'Identificador único UUID del evento de auditoría';
COMMENT ON COLUMN auditoria_envios.envio_id IS 'FK a envios: envío que fue modificado';
COMMENT ON COLUMN auditoria_envios.accion IS 'Tipo de acción: CREATE, UPDATE, DELETE, ESTADO_CAMBIO, etc.';
COMMENT ON COLUMN auditoria_envios.usuario_id_externo IS 'UUID del usuario Auth que realizó la acción';
COMMENT ON COLUMN auditoria_envios.valores_anteriores IS 'JSONB con estado anterior después del cambio';
COMMENT ON COLUMN auditoria_envios.valores_nuevos IS 'JSONB con nuevo estado des{pués del cambio';
COMMENT ON COLUMN auditoria_envios.created_at IS 'Timestamp del evento de auditoría';

-- ==================== RESTRICCIONES ADICIONALES ====================

-- Validar peso positivo
ALTER TABLE envios 
ADD CONSTRAINT ck_envios_peso_positivo 
CHECK (peso_kg IS NULL OR peso_kg > 0);

-- Validar que fecha estimada es después de creación
ALTER TABLE envios 
ADD CONSTRAINT ck_envios_fecha_consistencia 
CHECK (fecha_estimada_entrega IS NULL OR fecha_estimada_entrega >= fecha_creacion::date);

-- Validar que departamento es Antioquia
ALTER TABLE direcciones 
ADD CONSTRAINT ck_direcciones_departamento 
CHECK (departamento = 'Antioquia');

-- Validar que país es Colombia
ALTER TABLE direcciones 
ADD CONSTRAINT ck_direcciones_pais 
CHECK (pais = 'Colombia');

-- Validar formato básico de email en remitentes
ALTER TABLE remitentes 
ADD CONSTRAINT ck_remitentes_email_format 
CHECK (email IS NULL OR email LIKE '%@%.%');

-- Validar formato básico de email en destinatarios
ALTER TABLE destinatarios 
ADD CONSTRAINT ck_destinatarios_email_format 
CHECK (email IS NULL OR email LIKE '%@%.%');

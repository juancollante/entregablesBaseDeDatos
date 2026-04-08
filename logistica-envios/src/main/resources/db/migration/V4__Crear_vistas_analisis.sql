-- V4__Crear_vistas_analisis.sql
-- Migración: Crear vistas para análisis y reportes
-- Versión: 4
-- Fecha: 2026-04-07
-- Descripción: Vistas optimizadas para consultas comunes y reportes analíticos

-- ==================== VISTA: ENVÍOS COMPLETOS ====================
-- Propósito: Obtener información completa de un envío con todos sus detalles
-- Uso: Búsqueda de envío por guía, perfil de envío en UI

DROP VIEW IF EXISTS vw_envios_completos CASCADE;

CREATE VIEW vw_envios_completos AS
SELECT 
    e.id,
    e.numero_guia,
    e.fecha_creacion,
    ee.codigo AS estado_codigo,
    ee.nombre AS estado_nombre,
    ee.descripcion AS estado_descripcion,
    
    -- Datos remitente
    r.id AS remitente_id,
    r.nombre_completo AS remitente_nombre,
    r.documento AS remitente_documento,
    r.email AS remitente_email,
    r.telefono AS remitente_telefono,
    
    -- Dirección remitente
    dr.linea1 AS remitente_linea1,
    dr.linea2 AS remitente_linea2,
    dr.municipio_codigo_dane AS remitente_dane,
    dr.municipio_nombre AS remitente_municipio,
    dr.codigo_postal AS remitente_codigo_postal,
    dr.referencias AS remitente_referencias,
    
    -- Datos destinatario
    d.id AS destinatario_id,
    d.nombre_completo AS destinatario_nombre,
    d.documento AS destinatario_documento,
    d.email AS destinatario_email,
    d.telefono AS destinatario_telefono,
    
    -- Dirección destinatario
    dd.linea1 AS destinatario_linea1,
    dd.linea2 AS destinatario_linea2,
    dd.municipio_codigo_dane AS destinatario_dane,
    dd.municipio_nombre AS destinatario_municipio,
    dd.codigo_postal AS destinatario_codigo_postal,
    dd.referencias AS destinatario_referencias,
    
    -- Datos envío
    e.descripcion_paquete,
    e.peso_kg,
    e.fecha_estimada_entrega,
    e.codigo_sede_registro,
    e.creado_por_usuario_id_externo,
    e.updated_at
FROM envios e
LEFT JOIN estados_envio ee ON e.estado_envio_id = ee.id
LEFT JOIN remitentes r ON e.remitente_id = r.id
LEFT JOIN direcciones dr ON r.direccion_id = dr.id
LEFT JOIN destinatarios d ON e.destinatario_id = d.id
LEFT JOIN direcciones dd ON d.direccion_id = dd.id
ORDER BY e.fecha_creacion DESC;

COMMENT ON VIEW vw_envios_completos IS 'Vista con información completa de envíos incluyendo datos de remitente, destinatario y estados. Usada para búsquedas detalladas y display de perfil de envío.';

-- ==================== VISTA: ENVÍOS POR ESTADO ====================
-- Propósito: Contar envíos activos por estado
-- Uso: Dashboard, métricas operacionales

DROP VIEW IF EXISTS vw_envios_por_estado CASCADE;

CREATE VIEW vw_envios_por_estado AS
SELECT 
    ee.id AS estado_id,
    ee.codigo AS estado_codigo,
    ee.nombre AS estado_nombre,
    COUNT(e.id) AS cantidad_envios,
    MIN(e.fecha_creacion) AS primer_envio,
    MAX(e.fecha_creacion) AS ultimo_envio
FROM estados_envio ee
LEFT JOIN envios e ON ee.id = e.estado_envio_id
GROUP BY ee.id, ee.codigo, ee.nombre
ORDER BY COUNT(e.id) DESC;

COMMENT ON VIEW vw_envios_por_estado IS 'Cuantos envíos hay en cada estado. Útil para dashboards de operación y métricas en tiempo real.';

-- ==================== VISTA: ENVÍOS POR MUNICIPIO ====================
-- Propósito: Análisis de envíos por zona geográfica
-- Uso: Reportes por región, análisis de cobertura

DROP VIEW IF EXISTS vw_envios_por_municipio_destino CASCADE;

CREATE VIEW vw_envios_por_municipio_destino AS
SELECT 
    dd.municipio_codigo_dane,
    dd.municipio_nombre,
    ee.codigo AS estado_codigo,
    ee.nombre AS estado_nombre,
    COUNT(e.id) AS cantidad_envios,
    AVG(e.peso_kg) AS peso_promedio_kg,
    MIN(e.fecha_creacion) AS primer_envio,
    MAX(e.fecha_creacion) AS ultimo_envio_registrado
FROM envios e
LEFT JOIN estados_envio ee ON e.estado_envio_id = ee.id
LEFT JOIN destinatarios d ON e.destinatario_id = d.id
LEFT JOIN direcciones dd ON d.direccion_id = dd.id
WHERE dd.municipio_nombre IS NOT NULL
GROUP BY dd.municipio_codigo_dane, dd.municipio_nombre, ee.codigo, ee.nombre
ORDER BY dd.municipio_nombre, cantidad_envios DESC;

COMMENT ON VIEW vw_envios_por_municipio_destino IS 'Distribución de envíos por municipio destino y estado actual. Análisis geográfico de operación.';

-- ==================== VISTA: HISTORIAL DE CAMBIOS DE ESTADO ====================
-- Propósito: Rastreo temporal de cambios de estado
-- Uso: Análisis de SLA, velocidad de procesamiento

DROP VIEW IF EXISTS vw_historial_cambios_estado CASCADE;

CREATE VIEW vw_historial_cambios_estado AS
SELECT 
    a.envio_id,
    e.numero_guia,
    a.id AS auditoria_id,
    a.usuario_id_externo,
    a.created_at,
    e.estado_envio_id AS estado_actual_id,
    ee.nombre AS estado_actual_nombre,
    -- Extrae estado anterior del JSON
    (a.valores_anteriores ->> 'estado_envio_id')::UUID AS estado_anterior_id,
    -- Calcula tiempo entre cambios
    EXTRACT(HOUR FROM a.created_at - LAG(a.created_at) 
        OVER (PARTITION BY a.envio_id ORDER BY a.created_at)) AS horas_desde_cambio_anterior
FROM auditoria_envios a
LEFT JOIN envios e ON a.envio_id = e.id
LEFT JOIN estados_envio ee ON e.estado_envio_id = ee.id
WHERE a.accion = 'ESTADO_CAMBIO'
ORDER BY a.envio_id, a.created_at;

COMMENT ON VIEW vw_historial_cambios_estado IS 'Línea de tiempo de cambios de estado por envío. Permite analizar velocidad de procesamiento y cumplimiento de SLA.';

-- ==================== VISTA: ENVÍOS CON RETRASO ====================
-- Propósito: Identificar envíos con potencial retraso en entrega
-- Uso: Alertas, administrativo

DROP VIEW IF EXISTS vw_envios_con_retraso CASCADE;

CREATE VIEW vw_envios_con_retraso AS
SELECT 
    e.id,
    e.numero_guia,
    e.fecha_creacion,
    e.fecha_estimada_entrega,
    CURRENT_DATE - e.fecha_estimada_entrega AS dias_retraso,
    ee.nombre AS estado_actual,
    d.nombre_completo AS destinatario_nombre,
    dd.municipio_nombre AS municipio_destino
FROM envios e
LEFT JOIN estados_envio ee ON e.estado_envio_id = ee.id
LEFT JOIN destinatarios d ON e.destinatario_id = d.id
LEFT JOIN direcciones dd ON d.direccion_id = dd.id
WHERE e.fecha_estimada_entrega IS NOT NULL
  AND CURRENT_DATE > e.fecha_estimada_entrega
  AND ee.codigo NOT IN ('ENTREGADO', 'INCIDENCIA')
ORDER BY dias_retraso DESC;

COMMENT ON VIEW vw_envios_con_retraso IS 'Envíos que pasaron su fecha estimada de entrega sin haber sido entregados. Alertas administrativas.';

-- ==================== VISTA: ESTADÍSTICAS DIARIAS ====================
-- Propósito: Métricas consolidadas por día
-- Uso: Reportes de operación, KPIs

DROP VIEW IF EXISTS vw_estadisticas_diarias CASCADE;

CREATE VIEW vw_estadisticas_diarias AS
SELECT 
    DATE(e.fecha_creacion) AS fecha,
    COUNT(e.id) AS envios_creados,
    COUNT(DISTINCT CASE WHEN ee.codigo = 'ENTREGADO' THEN e.id END) AS envios_entregados,
    COUNT(DISTINCT CASE WHEN ee.codigo = 'INCIDENCIA' THEN e.id END) AS envios_con_incidencia,
    ROUND(AVG(e.peso_kg)::NUMERIC, 2) AS peso_promedio_kg,
    COUNT(DISTINCT e.remitente_id) AS remitentes_unicos,
    COUNT(DISTINCT dd.municipio_codigo_dane) AS municipios_destino
FROM envios e
LEFT JOIN estados_envio ee ON e.estado_envio_id = ee.id
LEFT JOIN destinatarios d ON e.destinatario_id = d.id
LEFT JOIN direcciones dd ON d.direccion_id = dd.id
GROUP BY DATE(e.fecha_creacion)
ORDER BY fecha DESC;

COMMENT ON VIEW vw_estadisticas_diarias IS 'Métricas consolidadas por día: volumen de envíos, entregas, incidencias y cobertura. Usada para dashboard operacional.';

-- ==================== VISTA: AUDITORÍA DETALLADA ====================
-- Propósito: Historial completo de cambios con detalles
-- Uso: Compliance, investigación de incidentes

DROP VIEW IF EXISTS vw_auditoria_detallada CASCADE;

CREATE VIEW vw_auditoria_detallada AS
SELECT 
    a.id,
    a.envio_id,
    e.numero_guia,
    a.accion,
    a.usuario_id_externo,
    a.created_at,
    CASE 
        WHEN a.accion = 'CREATE' THEN 'Creación del envío'
        WHEN a.accion = 'UPDATE' THEN 'Actualización de datos'
        WHEN a.accion = 'DELETE' THEN 'Eliminación'
        WHEN a.accion = 'ESTADO_CAMBIO' THEN 'Cambio de estado'
        ELSE a.accion
    END AS descripcion_accion,
    jsonb_pretty(COALESCE(a.valores_anteriores, '{}'::JSONB)) AS valores_anteriores,
    jsonb_pretty(COALESCE(a.valores_nuevos, '{}'::JSONB)) AS valores_nuevos,
    (a.created_at - LAG(a.created_at) OVER (PARTITION BY a.envio_id ORDER BY a.created_at))::INTERVAL AS tiempo_desde_anterior
FROM auditoria_envios a
LEFT JOIN envios e ON a.envio_id = e.id
ORDER BY a.envio_id, a.created_at;

COMMENT ON VIEW vw_auditoria_detallada IS 'Auditoría completa formateada para investigación. Muestra todos los cambios con valores anteriores y nuevos en formato legible.';

-- ==================== VISTA: REMITENTES FRECUENTES ====================
-- Propósito: Identificar clientes activos
-- Uso: CRM, análisis de cliente

DROP VIEW IF EXISTS vw_remitentes_frecuentes CASCADE;

CREATE VIEW vw_remitentes_frecuentes AS
SELECT 
    r.id,
    r.nombre_completo,
    r.documento,
    r.email,
    r.telefono,
    COUNT(e.id) AS total_envios,
    COUNT(DISTINCT DATE(e.fecha_creacion)) AS dias_activo,
    MAX(e.fecha_creacion) AS ultimo_envio,
    ROUND(AVG(e.peso_kg)::NUMERIC, 2) AS peso_promedio_kg
FROM remitentes r
LEFT JOIN envios e ON r.id = e.remitente_id
GROUP BY r.id, r.nombre_completo, r.documento, r.email, r.telefono
HAVING COUNT(e.id) > 0
ORDER BY COUNT(e.id) DESC;

COMMENT ON VIEW vw_remitentes_frecuentes IS 'Remitentes ordenados por cantidad de envíos. Análisis de clientes principales para CRM y logística.';

-- ==================== VISTA: CALIDAD DE DATOS ====================
-- Propósito: Monitoreo de integridad de datos
-- Uso: Auditoría de datos, validación

DROP VIEW IF EXISTS vw_calidad_datos CASCADE;

CREATE VIEW vw_calidad_datos AS
SELECT 
    'Envios sin descripcion' AS validacion,
    COUNT(*) AS cantidad,
    ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM envios), 2) AS porcentaje
FROM envios
WHERE descripcion_paquete IS NULL OR descripcion_paquete = ''

UNION ALL

SELECT 
    'Envios sin peso',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM envios), 2)
FROM envios
WHERE peso_kg IS NULL

UNION ALL

SELECT 
    'Remitentes sin email',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM remitentes), 2)
FROM remitentes
WHERE email IS NULL OR email = ''

UNION ALL

SELECT 
    'Destinatarios sin email',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM destinatarios), 2)
FROM destinatarios
WHERE email IS NULL OR email = ''

UNION ALL

SELECT 
    'Direcciones sin codigo DANE',
    COUNT(*),
    ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM direcciones), 2)
FROM direcciones
WHERE municipio_codigo_dane IS NULL OR municipio_codigo_dane = ''

ORDER BY cantidad DESC;

COMMENT ON VIEW vw_calidad_datos IS 'Métricas de integridad de datos. Identifica registros incompletos o faltantes.';

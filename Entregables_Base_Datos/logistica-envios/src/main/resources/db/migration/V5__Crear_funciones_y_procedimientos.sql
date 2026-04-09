-- V5__Crear_funciones_y_procedimientos.sql
-- Migración: Crear funciones y procedimientos almacenados
-- Versión: 5
-- Fecha: 2026-04-07
-- Descripción: Procedimientos para operaciones comunes y lógica transaccional

-- ==================== FUNCIÓN: Generar Número de Guía ====================
-- Propósito: Generar número de guía único automáticamente
-- Parámetros: None
-- Retorna: VARCHAR(50)

CREATE OR REPLACE FUNCTION fn_generar_numero_guia()
RETURNS VARCHAR(50) AS $$
DECLARE
    v_numero_guia VARCHAR(50);
    v_contador INT;
BEGIN
    -- Formato: ECV-AAAA-NNNNNNN (Envíos Colombia - Año - Secuencia)
    -- Ejemplo: ECV-2026-0001234
    
    SELECT COUNT(*) INTO v_contador 
    FROM envios 
    WHERE DATE(fecha_creacion) = CURRENT_DATE;
    
    v_numero_guia := 'ECV-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || 
                     LPAD((v_contador + 1)::TEXT, 7, '0');
    
    -- Validar que no exista (por seguridad)
    IF EXISTS(SELECT 1 FROM envios WHERE numero_guia = v_numero_guia) THEN
        -- Generar UUID como fallback
        v_numero_guia := 'ECV-' || SUBSTRING(gen_random_uuid()::TEXT, 1, 12);
    END IF;
    
    RETURN v_numero_guia;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_generar_numero_guia() IS 'Genera número de guía único en formato ECV-AAAA-NNNNNNN. Se busca secuencia por día, con fallback a UUID.';

-- ==================== FUNCIÓN: Calcular Días en Estado ====================
-- Propósito: Calcula cuántos días un envío ha estado en su estado actual
-- Parámetros: envio_id (UUID)
-- Retorna: INT (días)

CREATE OR REPLACE FUNCTION fn_dias_en_estado_actual(p_envio_id UUID)
RETURNS INT AS $$
DECLARE
    v_dias INT;
    v_fecha_cambio TIMESTAMPTZ;
BEGIN
    -- Obtiene la fecha del último cambio de estado
    SELECT a.created_at INTO v_fecha_cambio
    FROM auditoria_envios a
    WHERE a.envio_id = p_envio_id 
      AND a.accion = 'ESTADO_CAMBIO'
    ORDER BY a.created_at DESC
    LIMIT 1;
    
    -- Si no hay cambio de estado registrado, usa fecha_creacion
    IF v_fecha_cambio IS NULL THEN
        SELECT EXTRACT(DAY FROM CURRENT_TIMESTAMP - e.fecha_creacion)::INT 
        INTO v_dias
        FROM envios e
        WHERE e.id = p_envio_id;
    ELSE
        SELECT EXTRACT(DAY FROM CURRENT_TIMESTAMP - v_fecha_cambio)::INT 
        INTO v_dias;
    END IF;
    
    RETURN COALESCE(v_dias, 0);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_dias_en_estado_actual(UUID) IS 'Retorna cuántos días un envío ha permanecido en su estado actual.';

-- ==================== FUNCIÓN: Validar Email ====================
-- Propósito: Valida formato básico de email
-- Parámetros: p_email (VARCHAR)
-- Retorna: BOOLEAN

CREATE OR REPLACE FUNCTION fn_validar_email(p_email VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    -- Validación básica: debe contener @ y punto después del @
    RETURN p_email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$';
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_validar_email(VARCHAR) IS 'Valida que un email tenga formato básico válido (RFC 5322 simplificado).';

-- ==================== FUNCIÓN: Obtener Descripción Estado ====================
-- Propósito: Obtiene el estado legible de un envío
-- Parámetros: envio_id (UUID)
-- Retorna: TABLE con estado_codigo, estado_nombre, dias_en_estado

CREATE OR REPLACE FUNCTION fn_obtener_estado_envio(p_envio_id UUID)
RETURNS TABLE(
    estado_codigo VARCHAR,
    estado_nombre VARCHAR,
    dias_en_estado INT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ee.codigo,
        ee.nombre,
        fn_dias_en_estado_actual(p_envio_id)
    FROM envios e
    LEFT JOIN estados_envio ee ON e.estado_envio_id = ee.id
    WHERE e.id = p_envio_id;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION fn_obtener_estado_envio(UUID) IS 'Obtiene estado legible de un envío junto con días en estado actual.';

-- ==================== PROCEDIMIENTO: Cambiar Estado Envío ====================
-- Propósito: Cambia estado de envío con auditoría automática
-- Parámetros:
--   p_envio_id: UUID del envío
--   p_estado_codigo: Código nuevo estado (CREADO, EN_TRANSITO, etc.)
--   p_usuario_id: UUID del usuario que realiza cambio
-- Output: Success/Error message

CREATE OR REPLACE PROCEDURE sp_cambiar_estado_envio(
    p_envio_id UUID,
    p_estado_codigo VARCHAR,
    p_usuario_id UUID,
    OUT p_exito BOOLEAN,
    OUT p_mensaje VARCHAR
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_actual_id UUID;
    v_nuevo_estado_id UUID;
    v_numero_guia VARCHAR;
    v_error_msg VARCHAR;
BEGIN
    p_exito := FALSE;
    
    BEGIN
        -- Validar que envío existe
        SELECT id, numero_guia, estado_envio_id 
        INTO v_envio_id, v_numero_guia, v_estado_actual_id
        FROM envios
        WHERE id = p_envio_id
        FOR UPDATE; -- Lock para evitar race conditions
        
        IF v_envio_id IS NULL THEN
            p_mensaje := 'Envío no encontrado';
            RETURN;
        END IF;
        
        -- Obtener ID del nuevo estado
        SELECT id INTO v_nuevo_estado_id
        FROM estados_envio
        WHERE codigo = p_estado_codigo;
        
        IF v_nuevo_estado_id IS NULL THEN
            p_mensaje := 'Estado no válido: ' || p_estado_codigo;
            RETURN;
        END IF;
        
        -- Evitar cambio a mismo estado
        IF v_estado_actual_id = v_nuevo_estado_id THEN
            p_mensaje := 'Envío ya está en estado ' || p_estado_codigo;
            RETURN;
        END IF;
        
        -- Actualizar estado
        UPDATE envios
        SET estado_envio_id = v_nuevo_estado_id,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = p_envio_id;
        
        -- Registrar en auditoría
        INSERT INTO auditoria_envios (
            id, envio_id, accion, usuario_id_externo, 
            valores_anteriores, valores_nuevos, created_at
        ) VALUES (
            gen_random_uuid(),
            p_envio_id,
            'ESTADO_CAMBIO',
            p_usuario_id,
            JSONB_BUILD_OBJECT('estado_envio_id', v_estado_actual_id::TEXT),
            JSONB_BUILD_OBJECT('estado_envio_id', v_nuevo_estado_id::TEXT),
            CURRENT_TIMESTAMP
        );
        
        p_exito := TRUE;
        p_mensaje := 'Estado cambió exitosamente a ' || p_estado_codigo || 
                     ' (guía: ' || v_numero_guia || ')';
        
    EXCEPTION WHEN OTHERS THEN
        p_exito := FALSE;
        p_mensaje := 'Error al cambiar estado: ' || SQLERRM;
    END;
END;
$$;

COMMENT ON PROCEDURE sp_cambiar_estado_envio(UUID, VARCHAR, UUID, BOOLEAN, VARCHAR) 
IS 'Cambia el estado de un envío. Incluye validaciones y registra en auditoría automáticamente.';

-- ==================== PROCEDIMIENTO: Crear Envío ====================
-- Propósito: Crea envío completo con datos de remitente y destinatario
-- Note: Más detallado sería mejor, pero mantiene simplificación para MVP

CREATE OR REPLACE PROCEDURE sp_crear_envio(
    p_remitente_id UUID,
    p_destinatario_id UUID,
    p_descripcion_paquete VARCHAR DEFAULT NULL,
    p_peso_kg NUMERIC DEFAULT NULL,
    p_codigo_sede_registro VARCHAR DEFAULT NULL,
    p_usuario_id UUID DEFAULT NULL,
    OUT p_envio_id UUID,
    OUT p_numero_guia VARCHAR,
    OUT p_exito BOOLEAN,
    OUT p_mensaje VARCHAR
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_creado_id UUID;
BEGIN
    p_exito := FALSE;
    p_envio_id := NULL;
    
    BEGIN
        -- Obtener ID del estado CREADO
        SELECT id INTO v_estado_creado_id
        FROM estados_envio
        WHERE codigo = 'CREADO'
        LIMIT 1;
        
        IF v_estado_creado_id IS NULL THEN
            p_mensaje := 'Estado CREADO no existe en BD';
            RETURN;
        END IF;
        
        -- Generar número de guía
        p_numero_guia := fn_generar_numero_guia();
        
        -- Crear envío
        INSERT INTO envios (
            id, numero_guia, estado_envio_id, remitente_id, destinatario_id,
            descripcion_paquete, peso_kg, codigo_sede_registro,
            creado_por_usuario_id_externo, fecha_creacion, updated_at
        ) VALUES (
            gen_random_uuid(),
            p_numero_guia,
            v_estado_creado_id,
            p_remitente_id,
            p_destinatario_id,
            p_descripcion_paquete,
            p_peso_kg,
            p_codigo_sede_registro,
            p_usuario_id,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        ) RETURNING id INTO p_envio_id;
        
        -- Registrar creación en auditoría
        INSERT INTO auditoria_envios (
            id, envio_id, accion, usuario_id_externo, valores_nuevos, created_at
        ) VALUES (
            gen_random_uuid(),
            p_envio_id,
            'CREATE',
            p_usuario_id,
            JSONB_BUILD_OBJECT(
                'numero_guia', p_numero_guia,
                'descripcion_paquete', p_descripcion_paquete,
                'peso_kg', p_peso_kg
            ),
            CURRENT_TIMESTAMP
        );
        
        p_exito := TRUE;
        p_mensaje := 'Envío creado exitosamente con guía: ' || p_numero_guia;
        
    EXCEPTION WHEN OTHERS THEN
        p_exito := FALSE;
        p_envio_id := NULL;
        p_mensaje := 'Error al crear envío: ' || SQLERRM;
    END;
END;
$$;

COMMENT ON PROCEDURE sp_crear_envio(UUID, UUID, VARCHAR, NUMERIC, VARCHAR, UUID, UUID, VARCHAR, BOOLEAN, VARCHAR)
IS 'Crea un nuevo envío completo. Genera guía única, auditoría automática y valida integridad.';

-- ==================== PROCEDIMIENTO: Limpiar Auditoría Antigua ====================
-- Propósito: Elimina registros de auditoría más antiguos que X días (compliance)
-- Uso: Llamar con cron mensualmente

CREATE OR REPLACE PROCEDURE sp_limpiar_auditoria_antigua(
    p_dias_retencion INT DEFAULT 365,
    OUT p_registros_eliminados INT,
    OUT p_exito BOOLEAN,
    OUT p_mensaje VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    p_exito := FALSE;
    
    BEGIN
        DELETE FROM auditoria_envios
        WHERE created_at < CURRENT_TIMESTAMP - (p_dias_retencion || ' days')::INTERVAL;
        
        GET DIAGNOSTICS p_registros_eliminados = ROW_COUNT;
        
        p_exito := TRUE;
        p_mensaje := 'Se eliminaron ' || p_registros_eliminados || 
                     ' registros de auditoría con antigüedad > ' || p_dias_retencion || ' días';
        
    EXCEPTION WHEN OTHERS THEN
        p_exito := FALSE;
        p_registros_eliminados := 0;
        p_mensaje := 'Error al limpiar auditoría: ' || SQLERRM;
    END;
END;
$$;

COMMENT ON PROCEDURE sp_limpiar_auditoria_antigua(INT, INT, BOOLEAN, VARCHAR)
IS 'Limpia registros muy antiguos de auditoría por compliance. Retiene N días (default 365 = 1 año).';

-- ==================== PROCEDIMIENTO: Generar Reporte Diario ====================
-- Propósito: Genera valores para dashboard operacional del día

CREATE OR REPLACE PROCEDURE sp_generar_reporte_diario(
    OUT p_envios_creados INT,
    OUT p_envios_entregados INT,
    OUT p_envios_con_incidencia INT,
    OUT p_municipios_destino INT,
    OUT p_peso_total_kg NUMERIC,
    OUT p_exito BOOLEAN
)
LANGUAGE plpgsql
AS $$
BEGIN
    BEGIN
        SELECT 
            COUNT(DISTINCT CASE WHEN DATE(e.fecha_creacion) = CURRENT_DATE THEN e.id END),
            COUNT(DISTINCT CASE WHEN DATE(e.fecha_creacion) = CURRENT_DATE AND ee.codigo = 'ENTREGADO' THEN e.id END),
            COUNT(DISTINCT CASE WHEN DATE(e.fecha_creacion) = CURRENT_DATE AND ee.codigo = 'INCIDENCIA' THEN e.id END),
            COUNT(DISTINCT CASE WHEN DATE(e.fecha_creacion) = CURRENT_DATE THEN dd.municipio_codigo_dane END),
            SUM(CASE WHEN DATE(e.fecha_creacion) = CURRENT_DATE THEN e.peso_kg ELSE 0 END)
        INTO 
            p_envios_creados,
            p_envios_entregados,
            p_envios_con_incidencia,
            p_municipios_destino,
            p_peso_total_kg
        FROM envios e
        LEFT JOIN estados_envio ee ON e.estado_envio_id = ee.id
        LEFT JOIN destinatarios d ON e.destinatario_id = d.id
        LEFT JOIN direcciones dd ON d.direccion_id = dd.id;
        
        p_exito := TRUE;
        
    EXCEPTION WHEN OTHERS THEN
        p_exito := FALSE;
        p_envios_creados := 0;
        p_envios_entregados := 0;
        p_envios_con_incidencia := 0;
        p_municipios_destino := 0;
        p_peso_total_kg := 0;
    END;
END;
$$;

COMMENT ON PROCEDURE sp_generar_reporte_diario(INT, INT, INT, INT, NUMERIC, BOOLEAN)
IS 'Genera métricas de operación del día actual para dashboard. Llamable desde API o cron.';

-- ==================== ÍNDICES ADICIONALES PARA FUNCIONES ====================
-- Para búsquedas en auditoria por acción y usuario

CREATE INDEX IF NOT EXISTS idx_auditoria_accion_fecha 
  ON auditoria_envios (accion, created_at DESC)
  WHERE accion = 'ESTADO_CAMBIO';

COMMENT ON INDEX idx_auditoria_accion_fecha IS 'Índice para búsqueda rápida de cambios de estado por fecha.';

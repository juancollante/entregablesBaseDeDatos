# Documentación Completa: Modelo de Base de Datos Envíos
## Sistema: logistica-envios | Fecha: 2026-04-07

---

## 📋 Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Arquitectura del Modelo](#arquitectura-del-modelo)
3. [Tablas Principales](#tablas-principales)
4. [Migraciones Flyway](#migraciones-flyway)
5. [Vistas de Análisis](#vistas-de-análisis)
6. [Procedimientos y Funciones](#procedimientos-y-funciones)
7. [Índices de Rendimiento](#índices-de-rendimiento)
8. [Casos de Uso](#casos-de-uso)
9. [Integraciones](#integraciones)
10. [Guía de Implementación](#guía-de-implementación)

---

## 📖 Resumen Ejecutivo

### Propósito
Modelo de datos normalizado para gestión de envíos (logística) con trazabilidad, auditoría y análisis operacional.

### Características Clave
- **Base de Datos**: PostgreSQL 12+ (columna JSONB para auditoría)
- **Tablas Principales**: 6 tablas normalizadas
- **Vistas Analíticas**: 8 vistas para reportes y dashboards
- **Funciones**: 4 funciones reutilizables
- **Procedimientos**: 4 procedimientos para operaciones críticas
- **Auditoría**: Tabla `auditoria_envios` con historial completo
- **Identificadores**: UUID para escalabilidad distribuida

### Datos Actuales
- V1: Esquema base con 6 tablas
- V2: Datos de referencia (estados)
- V3: Índices y restricciones (próximo deploy)
- V4: Vistas analíticas (próximo deploy)
- V5: Funciones y procedimientos (próximo deploy)

---

## 🏗️ Arquitectura del Modelo

### Diagrama de Relaciones (simplificado)

```
┌─────────────────────────────────────────────────────────────┐
│                    ESTADOS_ENVIO                           │
│  (Catálogo: CREADO, EN_TRANSITO, EN_SEDE, ENTREGADO, etc) │
└───────────────────────┬─────────────────────────────────────┘
                        │ FK: 1:N
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                       ENVIOS (Principal)                    │
│  Guía única, estado, remitente, destinatario, peso, fecha  │
└──┬──────────────────────────────┬──────────────────────────┬┘
   │ FK: N:1                       │ FK: N:1                │ FK: N:1
   ▼                               ▼                        ▼
┌──────────────┐            ┌──────────────┐        ┌──────────────┐
│  REMITENTES  │            │ DESTINATARIOS│        │  DIRECCIONES │
│  (Quién      │            │  (Quién      │        │  (Ubicación) │
│   envía)     │            │   recibe)    │        │ Municipios:  │
└──────────────┘            └──────────────┘        │ Antioquia &  │
       │ FK: N:1                 │ FK: N:1          │ Colombia     │
       └─────────────────────────┴──────────────────┘

AUDITORÍA:
┌─────────────────────────────────────────────────────────────┐
│           AUDITORIA_ENVIOS (Trazabilidad)                   │
│  Registra CREATE, UPDATE, DELETE, ESTADO_CAMBIO con JSONB   │
└──────────────────────────────────────────────────────────────┘
             │ FK: N:1 (CASCADE DELETE)
             ▼
       (Referencia a ENVIOS)
```

### Niveles de Relación

| Nivel | Descripción | Impacto |
|-------|-------------|--------|
| **Core** | ENVIOS, ESTADOS_ENVIO, REMITENTES, DESTINATARIOS, DIRECCIONES | Datos operacionales |
| **Auditoría** | AUDITORIA_ENVIOS | Historial y compliance |
| **Integración** | Referencias externas (sin FK): usuario_id, codigo_sede | Servicios independientes |

---

## 📊 Tablas Principales

### 1. ESTADOS_ENVIO (Catálogo)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID PK | Identificador único |
| codigo | VARCHAR(50) UNIQUE | CREADO, EN_TRANSITO, EN_SEDE, ENTREGADO, INCIDENCIA |
| nombre | VARCHAR(120) | Nombre legible |
| descripcion | VARCHAR(500) | Descripción detallada |

**Cardinalidad**: 1:N (un estado → múltiples envíos)
**Mutabilidad**: Baja (cambios muy infrecuentes)
**Índices**: `codigo` (búsqueda rápida)

---

### 2. DIRECCIONES (Normalizado)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID PK | Identificador único |
| linea1 | VARCHAR(255) NOT NULL | Dirección principal |
| linea2 | VARCHAR(255) NULL | Dirección alternativa (apto, casa) |
| municipio_codigo_dane | VARCHAR(5) NULL | Código DANE (validar externamente) |
| municipio_nombre | VARCHAR(120) NOT NULL | Nombre para display |
| departamento | VARCHAR(80) DEFAULT 'Antioquia' | Fijo operativo |
| pais | VARCHAR(80) DEFAULT 'Colombia' | Fijo |
| codigo_postal | VARCHAR(20) NULL | Opcional |
| referencias | VARCHAR(500) NULL | Notas: frente a..., etc. |

**Cardinalidad**: 1:N (una dir → múltiples remitentes/destinatarios)
**Mutabilidad**: Media (cambios ocasionales)
**Índices**: `municipio_codigo_dane`, `municipio_nombre`
**Validación**: DANE (sin FK a BD externa)

---

### 3. REMITENTES

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID PK | Identificador único |
| nombre_completo | VARCHAR(200) NOT NULL | Nombre del remitente |
| documento | VARCHAR(50) NULL | Cédula o pasaporte |
| email | VARCHAR(255) NULL | Validado con CHECK |
| telefono | VARCHAR(50) NULL | Número de contacto |
| direccion_id | UUID FK NOT NULL | Referencia a DIRECCIONES |

**Cardinalidad**: 1:N (un remitente → múltiples envíos en potencial)
**Mutabilidad**: Baja (datos relativamente estáticos)
**Índices**: `documento`, `email`, `direccion_id`
**Frontend**: Usado en `PartyRequest/PartyResponse` DTO

---

### 4. DESTINATARIOS

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID PK | Identificador único |
| nombre_completo | VARCHAR(200) NOT NULL | Nombre del destinatario |
| documento | VARCHAR(50) NULL | Cédula o pasaporte |
| email | VARCHAR(255) NULL | Validado con CHECK |
| telefono | VARCHAR(50) NULL | Número de contacto |
| direccion_id | UUID FK NOT NULL | Referencia a DIRECCIONES |

**Cardinalidad**: 1:N (un destinatario → múltiples envíos en potencial)
**Mutabilidad**: Baja
**Índices**: `documento`, `email`, `direccion_id`
**Frontend**: Usado en `PartyRequest/PartyResponse` DTO

---

### 5. ENVIOS (Principal)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID PK | Identificador único |
| numero_guia | VARCHAR(32) UNIQUE NOT NULL | Guía única (ECV-AAAA-NNNNNNN) |
| estado_envio_id | UUID FK NOT NULL | Estado actual |
| remitente_id | UUID FK NOT NULL | Remitente |
| destinatario_id | UUID FK NOT NULL | Destinatario |
| descripcion_paquete | VARCHAR(500) NULL | Contenido |
| peso_kg | NUMERIC(10,2) NULL | Peso (CHECK > 0) |
| fecha_creacion | TIMESTAMPTZ NOT NULL | Creación |
| fecha_estimada_entrega | DATE NULL | SLA objetivo |
| codigo_sede_registro | VARCHAR(50) NULL | Sede (sin FK externa) |
| creado_por_usuario_id_externo | UUID NULL | Usuario Auth |
| updated_at | TIMESTAMPTZ NOT NULL | Última modificación |

**Cardinalidad**: N:1 a ESTADOS_ENVIO, REMITENTES, DESTINATARIOS
**Mutabilidad**: Alta (cambios de estado frecuentes)
**Índices**: `numero_guia`, `estado_envio_id`, `fecha_creacion`, compuestos
**Frontend**: Mapeado a `Shipment` entidad JPA → `ShipmentResponse` DTO
**Validaciones**: peso > 0, fecha_entrega >= fecha_creacion

---

### 6. AUDITORIA_ENVIOS

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID PK | Identificador evento |
| envio_id | UUID FK NOT NULL | Envío auditado |
| accion | VARCHAR(80) NOT NULL | CREATE, UPDATE, DELETE, ESTADO_CAMBIO |
| usuario_id_externo | UUID NULL | Usuario Auth (sin FK) |
| valores_anteriores | JSONB NULL | Estado anterior |
| valores_nuevos | JSONB NULL | Nuevo estado |
| created_at | TIMESTAMPTZ NOT NULL | Timestamp evento |

**Cardinalidad**: N:1 a ENVIOS (CASCADE DELETE)
**Mutabilidad**: Append-only (inserts solamente)
**Índices**: `envio_id`, `accion`, `usuario_id_externo`, `created_at`
**Propósito**: Cumplimiento legal, debugging, análisis de cambios
**Retención**: Mínimo 1 año (legal)

---

## 🚀 Migraciones Flyway

### Secuencia de Ejecución

```
V1__envios_schema.sql
├─ Crea: ESTADOS_ENVIO, DIRECCIONES, REMITENTES, DESTINATARIOS, ENVIOS, AUDITORIA_ENVIOS
└─ Índices básicos: estado_envio_id, fecha_creacion

V2__seed_estados_envio.sql
├─ Inserta: 5 estados base
└─ Datos de referencia estáticos

V3__Agregar_indices_y_comentarios.sql (NUEVO)
├─ Índices especializados para búsquedas
├─ Índices compuestos para queries comunes
├─ Restricciones CHECK (validaciones)
└─ Comentarios en tablas/columnas

V4__Crear_vistas_analisis.sql (NUEVO)
├─ vw_envios_completos: info consolidada
├─ vw_envios_por_estado: métricas por estado
├─ vw_envios_por_municipio_destino: geográfico
├─ vw_historial_cambios_estado: timeline
├─ vw_envios_con_retraso: alertas
├─ vw_estadisticas_diarias: KPIs
├─ vw_auditoria_detallada: compliance
├─ vw_remitentes_frecuentes: CRM
└─ vw_calidad_datos: integridad

V5__Crear_funciones_y_procedimientos.sql (NUEVO)
├─ Funciones:
│  ├─ fn_generar_numero_guia(): guía única
│  ├─ fn_dias_en_estado_actual(): SLA tracking
│  ├─ fn_validar_email(): validación
│  └─ fn_obtener_estado_envio(): estado legible
├─ Procedimientos:
│  ├─ sp_cambiar_estado_envio(): transacción con auditoría
│  ├─ sp_crear_envio(): creación completa
│  ├─ sp_limpiar_auditoria_antigua(): compliance
│  └─ sp_generar_reporte_diario(): dashboard
└─ Índices para funciones
```

### Ejecución Programada

```bash
# Flyway ejecuta automáticamente en inicio de aplicación
mvn flyway:migrate

# O manual:
flyway info                    # Ver estado
flyway validate               # Validar consistencia
flyway migrate                # Aplicar pending
flyway undo -Dfly way.target=4  # Rollback (si supported)
```

---

## 📈 Vistas de Análisis

### Propósito por Vista

```
vw_envios_completos
├─ Uso: Búsqueda detallada por guía
├─ Performance: JOIN x3 (Remitentes, Destinatarios, Direcciones)
└─ Campos retornados: 20+ (completa información)

vw_envios_por_estado
├─ Uso: Dashboard de operación (cuántos envíos en cada estado)
├─ Agregación: COUNT, MIN/MAX por estado
└─ Frecuencia: Tiempo real

vw_envios_por_municipio_destino
├─ Uso: Análisis geográfico y cobertura
├─ Agregación: Cantidad, peso promedio por municipio
└─ Segmentación: Por estado dentro de cada municipio

vw_historial_cambios_estado
├─ Uso: Timeline de cambios para SLA tracking
├─ Cálculo: EXTRACT HOUR entre cambios
└─ Análisis: Velocidad de procesamiento

vw_envios_con_retraso
├─ Uso: Alertas administrativas
├─ Filtro: fecha_estimada < CURRENT_DATE AND estado != ENTREGADO
└─ Orden: Por días de retraso DESC

vw_estadisticas_diarias
├─ Uso: KPIs operacionales (envíos/día, entregas, incidencias)
├─ Agregación: Por DATE(fecha_creacion)
└─ Métricas: Volumen, entregas, incidencias, cobertura

vw_auditoria_detallada
├─ Uso: Compliance y auditoría
├─ Formato: JSONB_PRETTY para legibilidad
└─ Análisis: Quién, qué, cuándo cambió

vw_remitentes_frecuentes
├─ Uso: CRM - clientes principales
├─ Ranking: Por cantidad de envíos
└─ Métricas: Días activos, último envío, peso promedio
```

---

## ⚙️ Procedimientos y Funciones

### Funciones (Cálculos Reutilizable)

```sql
-- Generar guía única
SELECT fn_generar_numero_guia() -- Retorna: 'ECV-2026-0001234'

-- Calcular días en estado
SELECT fn_dias_en_estado_actual(envio_id::UUID) -- Retorna: INT

-- Validar email
SELECT fn_validar_email('usuario@empresa.com') -- Retorna: BOOLEAN

-- Obtener estado legible
SELECT * FROM fn_obtener_estado_envio(envio_id::UUID) 
-- Retorna: estado_codigo, estado_nombre, dias_en_estado
```

### Procedimientos (Transacciones)

```sql
-- Cambiar estado con auditoría automática
CALL sp_cambiar_estado_envio(
    p_envio_id => envio_uuid,
    p_estado_codigo => 'EN_TRANSITO',
    p_usuario_id => usuario_uuid,
    p_exito => success,
    p_mensaje => message
);

-- Crear envío completo
CALL sp_crear_envio(
    p_remitente_id => remitente_uuid,
    p_destinatario_id => destinatario_uuid,
    p_descripcion_paquete => 'Textiles',
    p_peso_kg => 2.5,
    p_codigo_sede_registro => 'SED-MDE-001',
    p_usuario_id => usuario_uuid,
    p_envio_id => nuevo_envio_id,
    p_numero_guia => nueva_guia,
    p_exito => success,
    p_mensaje => message
);

-- Generar reporte diario
CALL sp_generar_reporte_diario(
    p_envios_creados => out_created,
    p_envios_entregados => out_delivered,
    p_envios_con_incidencia => out_issues,
    p_municipios_destino => out_coverage,
    p_peso_total_kg => out_weight,
    p_exito => success
);

-- Limpiar auditoría antigua (compliance)
CALL sp_limpiar_auditoria_antigua(
    p_dias_retencion => 365,  -- 1 año mínimo legal
    p_registros_eliminados => deleted_count,
    p_exito => success,
    p_mensaje => message
);
```

---

## 🚄 Índices de Rendimiento

### Categorización

**Índices Únicos**:
- `numero_guia`: Búsqueda O(1), evita duplicados

**Índices Primarios**:
- `estado_envio_id`: Filtrado por estado
- `fecha_creacion`: Rangos de fecha
- `documento` (remitentes/destinatarios): Búsqueda persona

**Índices Compuestos** (V3):
- `(estado_envio_id, fecha_creacion)`: Envíos por estado y fecha
- `(envio_id, created_at)`: Auditoría ordenada

**Índices de Acceso**:
- `municipio_codigo_dane`: Validación DANE
- `usuario_id_externo`: Auditoría por usuario
- `codigo_sede_registro`: Por sede

### Plan de Ejecución

```
Consulta: SELECT FROM envios WHERE estado = ? AND fecha > ?
Índice: idx_envios_estado_fecha
Modo: Index Range Scan
Estimado: 10-100 rows (rápido)
```

---

## 📋 Casos de Uso

### UC1: Registrar Nuevo Envío

```sql
-- Paso 1: Crear dirección remitente
INSERT INTO direcciones (...) VALUES (...) RETURNING id;

-- Paso 2: Crear remitente
INSERT INTO remitentes (direccion_id, ...) VALUES (...) RETURNING id;

-- Paso 3: Crear dirección destinatario
INSERT INTO direcciones (...) VALUES (...) RETURNING id;

-- Paso 4: Crear destinatario
INSERT INTO destinatarios (direccion_id, ...) VALUES (...) RETURNING id;

-- Paso 5: Crear envío (mejor: usar procedure)
CALL sp_crear_envio(remitente_id, destinatario_id, ...);
```

**Origen**: `CreateEnvioRequest` DTO → `ShipmentApplicationService`

---

### UC2: Cambiar Estado Envío

```sql
CALL sp_cambiar_estado_envio(
    p_envio_id,
    'EN_TRANSITO',  -- o 'EN_SEDE', 'ENTREGADO', 'INCIDENCIA'
    usuario_uuid
);
-- Automático: UPDATE envios + INSERT auditoria
```

**Origen**: API PUT `/envios/{id}` → `EnvioController`

---

### UC3: Buscar Envío por Guía

```sql
SELECT * FROM vw_envios_completos
WHERE numero_guia = 'ECV-2026-0001234';
```

**Performance**: Index lookup en `numero_guia`, muy rápido

---

### UC4: Dashboard Operacional

```sql
SELECT * FROM vw_envios_por_estado;
SELECT * FROM vw_estadisticas_diarias LIMIT 1;  -- Hoy
CALL sp_generar_reporte_diario(...);
```

**Frecuencia**: Agrega datos en tiempo real

---

### UC5: Historial de Auditoría

```sql
SELECT * FROM vw_auditoria_detallada
WHERE envio_id = uuid AND accion = 'ESTADO_CAMBIO'
ORDER BY created_at ASC;
```

**Propósito**: Compliance, debugging, investigación

---

## 🔗 Integraciones

### Servicios Externos (Sin FK)

| Campo | Servicio | Referencia | Validación |
|-------|----------|-----------|-----------|
| `creado_por_usuario_id_externo` | Auth Service | UUID usuario | En aplicación |
| `codigo_sede_registro` | Seguimiento Service | Código sede | Tabla externa |
| `municipio_codigo_dane` | DANE (API) | Código oficial | Validación externa |

### Mapeo: Entidades JPA ↔ DTOs

```
Entity (JPA)                DTOs (REST)
────────────────           ──────────────
Shipment                    ShipmentResponse
├─ remitente_id        ↔   ShipmentResponse.remitente: PartyResponse
├─ destinatario_id     ↔   ShipmentResponse.destinatario: PartyResponse
└─ direccion_id        ↔   PartyResponse.address: AddressResponse

Remitente              ↔   PartyRequest/PartyResponse
├─ nombre              ─   name
├─ documento           ─   documentNumber
├─ email               ─   email
├─ telefono            ─   phone
└─ direccion_id        ↔   address (nested)

Direccion             ↔   AddressRequest/AddressResponse
├─ linea1              ─   line1
├─ linea2              ─   line2 (optional)
├─ municipio_nombre    ─   municipality
└─ referencias         ─   references
```

---

## 📝 Guía de Implementación

### 1. **Setup Inicial**

```bash
# 1. PostgreSQL instalado (12+)
# 2. BD creada: createdb logistica_envios
# 3. Flyway migra automáticamente al arrancar Spring Boot
./mvnw spring-boot:run
```

### 2. **Deployment de Cambios**

```
DESARROLLO:
├─ Modificar/crear *.sql en src/main/resources/db/migration/
├─ Versionar: V{N}__descripcion.sql
├─ Test en local
└─ Commit y merge

STAGING/PROD:
├─ CI/CD ejecuta: mvn flyway:migrate
├─ Si error: revisar y revertir
└─ Validar: SELECT COUNT(*) FROM [tablas]
```

### 3. **Monitoreo**

```sql
-- Estado de migraciones
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

-- Tamaño de tablas
SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename))
FROM pg_tables WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Ejecución de índices
EXPLAIN ANALYZE SELECT * FROM envios WHERE estado_envio_id = ? AND fecha_creacion > ?;
```

### 4. **Optimización Futura**

```
├─ Particionamiento: envios por YEAR(fecha_creacion) si > 100M registros
├─ Archivado: Envíos ENTREGADO > 1 año a tabla histórica
├─ Caché: vw_envios_por_estado en Redis (refresh cada 5 min)
├─ Replicación: Read replicas para reportes
└─ Data Warehouse: ETL diario a Analytics BD
```

---

## ✅ Checklist de Validación

- [ ] Base de datos creada: `logistica_envios`
- [ ] Flyway configurado en `application.properties`
- [ ] V1 & V2 migraciones ejecutadas sin error
- [ ] Entidades JPA creadas con `@Entity`
- [ ] DTOs mapeados correctamente (ModelMapper o MapStruct)
- [ ] Tests unitarios para `ShipmentApplicationService`
- [ ] Endpoints REST en `EnvioController` implementados
- [ ] Seguridad JWT validada (`JwtAuthenticationFilter`)
- [ ] V3, V4, V5 migraciones deplojiadas
- [ ] Vistas probadas en queries manuales
- [ ] Procedimientos testeados desde aplicación
- [ ] Índices verificados con EXPLAIN
- [ ] Documentación actualizada en Wiki
- [ ] Backup configurado (diario)
- [ ] Monitoring activo (Prometheus + Grafana)

---

## 📞 Contacto & Escalada

**Base de Datos**:
- DBA: verificar `pg_stat_statements` para query lenta
- Índices: revisar utilización con `pg_stat_user_indexes`

**Aplicación**:
- Logger: `DEBUG` en `ShipmentApplicationService`
- Auditoría: revisar `auditoria_envios` para cambios inesperados

**Compliance**:
- Retención: script `sp_limpiar_auditoria_antigua` mensualmente
- Reportes: vistas analíticas en dashboard operacional

---

## 📚 Referencias Útiles

- Documentos en carpeta `/docs`:
  - `MODELO_ENTIDAD_RELACION.md` - Diagrama visual
  - `MODELO_LOGICO_SQL.md` - Especificación SQL completa
  - `REFERENCIA_RAPIDA_SCHEMA.md` - Quick lookup

- Ficheros Flyway:
  - `V1__envios_schema.sql` - Estructura base
  - `V2__seed_estados_envio.sql` - Datos de referencia
  - `V3__Agregar_indices_y_comentarios.sql` - Optimización
  - `V4__Crear_vistas_analisis.sql` - Reportes
  - `V5__Crear_funciones_y_procedimientos.sql` - Lógica BD

- Código Backend:
  - `EnvioController` - REST endpoints
  - `ShipmentApplicationService` - Lógica de negocio
  - DTOs: `ShipmentResponse`, `CreateEnvioRequest`, etc.

---

**Última actualización**: 2026-04-07  
**Versión mod**: 5 (Funciones y Procedimientos)  
**Estado**: ✅ Listo para implementación

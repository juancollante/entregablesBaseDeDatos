# Especificación SQL - Formato Comprimido
## Sistema de Gestión de Envíos - logistica-envios

---

## Resumen de Tablas (Quick Reference)

### 1. ESTADOS_ENVIO
Estado actual del paquete en el sistema.

```
Campos: id, codigo (UNIQUE), nombre, descripcion
Estados Fijos: CREADO, EN_TRANSITO, EN_SEDE, ENTREGADO, INCIDENCIA
Relaciones: 1:N con ENVIOS
```

### 2. DIRECCIONES
Dirección normalizada para remitentes y destinatarios.

```
Campos: id, linea1, linea2, municipio_codigo_dane, municipio_nombre,
         departamento (Antioquia), pais (Colombia), codigo_postal,
         referencias, created_at, updated_at
Relaciones: 1:N con REMITENTES y DESTINATARIOS
Validación: DANE y municipios (sin FK a tabla externa)
```

### 3. REMITENTES (1:1 con ENVIOS en MVP)
Datos del remitente del envío.

```
Campos: id, nombre_completo, documento, email, telefono,
         direccion_id (FK), created_at, updated_at
Relaciones: N:1 con DIRECCIONES
          : 1:N con ENVIOS (potential)
Validación: Email con CHECK
```

### 4. DESTINATARIOS (1:1 con ENVIOS en MVP)
Datos del destinatario del envío.

```
Campos: id, nombre_completo, documento, email, telefono,
         direccion_id (FK), created_at, updated_at
Relaciones: N:1 con DIRECCIONES
          : 1:N con ENVIOS (potential)
Validación: Email con CHECK
```

### 5. ENVIOS
Cabecera del envío con estado y partes relacionadas.

```
Campos: id, numero_guia (UNIQUE), estado_envio_id (FK),
         remitente_id (FK), destinatario_id (FK),
         descripcion_paquete, peso_kg,
         fecha_creacion, fecha_estimada_entrega,
         codigo_sede_registro, creado_por_usuario_id_externo (UUID),
         created_at, updated_at
Relaciones: N:1 con ESTADOS_ENVIO
          : N:1 con REMITENTES
          : N:1 con DESTINATARIOS
          : 1:N con AUDITORIA_ENVIOS (CASCADE DELETE)
Validación: peso_kg > 0, fecha_entrega >= fecha_creacion
Índices: numero_guia, estado_envio_id, fecha_creacion, (estado, fecha)
```

### 6. AUDITORIA_ENVIOS (Trazabilidad)
Registro de cambios en envíos para auditoría.

```
Campos: id, envio_id (FK), accion (CREATE, UPDATE, DELETE, ESTADO_CAMBIO),
         usuario_id_externo (UUID), valores_anteriores (JSON),
         valores_nuevos (JSON), created_at
Relaciones: N:1 con ENVIOS (CASCADE DELETE)
Índices: envio_id, accion, usuario_id, created_at
```

---

## Diagramas de Cardinalidad Simplificados

```
ESTADOS_ENVIO (1) ────────── (N) ENVIOS
                 PK           FK: estado_envio_id

REMITENTES (1) ────────────── (1) ENVIOS
             PK                FK: remitente_id
             
DESTINATARIOS (1) ────────── (1) ENVIOS
              PK               FK: destinatario_id

DIRECCIONES (1) ──────── (N) REMITENTES
            PK           FK: direccion_id

DIRECCIONES (1) ──────── (N) DESTINATARIOS
            PK           FK: direccion_id

ENVIOS (1) ──────── (N) AUDITORIA_ENVIOS
       PK            FK: envio_id
```

---

## Restricciones Principales

| Restricción | Tipo | Detalle |
|-------------|------|---------|
| PK | Clave Primaria | Todas las tablas tienen id AUTOINCREMENT |
| UK | Clave Única | numero_guia en ENVIOS, codigo en ESTADOS_ENVIO |
| FK | Referencial | Con RESTRICT DELETE para integridad (CASCADE solo para auditoría) |
| CK | Check | peso_kg > 0, email formato, pais = Colombia, departamento = Antioquia |
| NOT NULL | Dominio | Campos obligatorios según negocio |
| NULL | Dominio | Campos opcionales (segundo contacto, cédula, etc.) |

---

## Ejemplo: Crear Envío Completo

### Paso 1: Crear Dirección Remitente
```sql
INSERT INTO direcciones (linea1, linea2, municipio_codigo_dane, municipio_nombre)
VALUES ('Carrera 50 #25-101', 'Apto 401', '05001', 'Medellín');
-- Retorna: direccion_id = 1
```

### Paso 2: Crear Remitente
```sql
INSERT INTO remitentes (nombre_completo, documento, email, telefono, direccion_id)
VALUES ('Juan Pérez García', '1001234567', 'juan@email.com', '3001234567', 1);
-- Retorna: remitente_id = 1
```

### Paso 3: Crear Dirección Destinatario
```sql
INSERT INTO direcciones (linea1, municipio_codigo_dane, municipio_nombre)
VALUES ('Calle 80 #45-30', '05002', 'Bello');
-- Retorna: direccion_id = 2
```

### Paso 4: Crear Destinatario
```sql
INSERT INTO destinatarios (nombre_completo, documento, email, telefono, direccion_id)
VALUES ('María López Rodríguez', '1005678901', 'maria@email.com', '3005678901', 2);
-- Retorna: destinatario_id = 1
```

### Paso 5: Crear Envío
```sql
INSERT INTO envios (
    numero_guia, estado_envio_id, remitente_id, destinatario_id,
    descripcion_paquete, peso_kg, codigo_sede_registro, creado_por_usuario_id_externo
) VALUES (
    'ECV-2026-001234',
    1, -- CREADO
    1, -- Juan Pérez
    1, -- María López
    'Textiles: 2 camisetas, 1 pantalón',
    2.5,
    'SED-MDE-001',
    'a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6'
);
-- Retorna: envio_id = 1
```

### Paso 6: Registrar Auditoría Automáticamente
```sql
INSERT INTO auditoria_envios (envio_id, accion, usuario_id_externo, valores_nuevos)
VALUES (
    1,
    'CREATE',
    'a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6',
    JSON_OBJECT(
        'numero_guia', 'ECV-2026-001234',
        'descripcion', 'Textiles: 2 camisetas, 1 pantalón',
        'peso_kg', 2.5
    )
);
```

### Paso 7: Cambiar Estado
```sql
UPDATE envios
SET estado_envio_id = 2, updated_at = CURRENT_TIMESTAMP
WHERE id = 1;

INSERT INTO auditoria_envios (envio_id, accion, usuario_id_externo, valores_anteriores, valores_nuevos)
VALUES (
    1,
    'ESTADO_CAMBIO',
    'a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6',
    JSON_OBJECT('estado_envio_id', 1),
    JSON_OBJECT('estado_envio_id', 2)
);
```

---

## Máximos Recomendados

| Parámetro | Límite | Notas |
|-----------|--------|-------|
| Longitud nombre | 255 | VARCHAR(255) en BD |
| Longitud dirección | 255 por línea | Soporta dirección larga |
| Número de envíos | Millones (sin problemas) | InnoDB + índices |
| Retención auditoría | 1 año mínimo | Por ley |
| Peso máximo | 9999.99 kg | DECIMAL(10,2) |

---

## Orden de Creación de Objetos

```
1. Base de datos (UTF8MB4)
   ↓
2. Tabla: ESTADOS_ENVIO (datos base)
   ↓
3. Tabla: DIRECCIONES
   ↓
4. Tabla: REMITENTES → FK a DIRECCIONES
   ↓
5. Tabla: DESTINATARIOS → FK a DIRECCIONES
   ↓
6. Tabla: ENVIOS → FK a ESTADOS_ENVIO, REMITENTES, DESTINATARIOS
   ↓
7. Tabla: AUDITORIA_ENVIOS → FK a ENVIOS
   ↓
8. Índices adicionales
   ↓
9. Vistas (vw_envios_completos, vw_envios_por_municipio, etc.)
   ↓
10. Procedimientos almacenados (sp_cambiar_estado_envio, sp_crear_envio)
```

---

## Preguntas Frecuentes de Diseño

### ¿Por qué DIRECCIONES es tabla separada?
- **Normalización**: Evita duplicación de direcciones idénticas
- **Mantenimiento**: Un cambio en dirección se propaga automáticamente
- **Búsqueda**: Índices sobre municipios, código postal, etc.
- **Reutilización**: Diferentes remitentes pueden compartir dirección

### ¿Por qué AUDITORIA_ENVIOS con JSONB?
- **Flexibilidad**: Captura cambios en cualquier campo sin columnas fijas
- **Rendimiento**: No requiere denormalización
- **Análisis**: Fácil de consultar con funciones JSON de MySQL

### ¿Por qué creado_por_usuario_id_externo es UUID?
- **Integración**: Referencia al sistema de Autenticación (Auth Service)
- **Descentralizado**: No mantiene FK a tabla externa (servicios independientes)
- **Auditoría**: Rastreos exacto quién creó/modificó cada envío

### ¿Qué pasa si se intenta eliminar un remitente con envíos?
- **Respuesta**: Falla con error de integridad referencial (ON DELETE RESTRICT)
- **Motivo**: Preservar historial e integridad de datos
- **Solución**: Marcar remitente como inactivo (agregar columna activo = FALSE)

### ¿Cómo hacer reportes sin afectar producción?
- **Opción 1**: Replicas read-only (requerida para volumen grande)
- **Opción 2**: Data warehouse con ETL diario
- **Opción 3**: Vistas optimizadas para reportes (incluidas en DDL)

---

## Migración desde Versión Anterior (Flyway)

```sql
-- V3__Agregar_auditoria_envios.sql
-- Agregar tabla de auditoría a esquema existente

ALTER TABLE envios ADD COLUMN creado_por_usuario_id_externo CHAR(36) NULL;
ALTER TABLE envios ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

CREATE TABLE auditoria_envios (
    -- ... ver especificación completa en MODELO_LOGICO_SQL.md
);
```

---

## Validaciones a Nivel de Aplicación (Spring Boot)

### Entidad: Shipment
```java
@Entity
@Table(name = "envios")
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String numeroGuia;
    
    @NotNull
    @Positive
    private BigDecimal pesoKg;
    
    @Email
    private String emailRemitente;
    
    @Email
    private String emailDestinatario;
    
    // ... validaciones del resto de campos
}
```

### Validación de Direcciones
```java
@PostMapping("/envios")
public ResponseEntity<?> createShipment(@Valid @RequestBody CreateEnvioRequest request) {
    // Validar que municipio_codigo_dane sea válido
    validateDaneCode(request.getFechaEstimadaEntrega());
    // ...
}
```

---

## Checklist de Implementación

- [ ] Base de datos creada con charset UTF8MB4
- [ ] Tabla ESTADOS_ENVIO con datos de referencia
- [ ] Tabla DIRECCIONES con índices en municipio
- [ ] Tabla REMITENTES con FK a DIRECCIONES
- [ ] Tabla DESTINATARIOS con FK a DIRECCIONES
- [ ] Tabla ENVIOS con todas las FK y validaciones
- [ ] Tabla AUDITORIA_ENVIOS con índices
- [ ] Vistas creadas (vw_envios_completos, etc.)
- [ ] Procedimientos almacenados implementados
- [ ] Índices especializados créados
- [ ] Datos de test insertados
- [ ] Backups configurados
- [ ] Documentación actualizada en wiki

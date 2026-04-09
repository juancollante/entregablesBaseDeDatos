# Diagrama Entidad-Relación (DER)
## Sistema de Gestión de Envíos - logistica-envios

---

## 1. Diagrama ER Conceptual

```
┌──────────────────┐
│ ESTADOS_ENVIO    │
├──────────────────┤
│ PK: id           │
│ codigo (UNIQUE)  │
│ nombre           │
│ descripcion      │
└────────┬─────────┘
         │
         │ (1:N)
         │
         ▼
┌──────────────────────┐
│      ENVIOS          │
├──────────────────────┤
│ PK: id               │
│ numero_guia (UNIQUE) │
│ FK: estado_envio_id  │
│ FK: remitente_id     │
│ FK: destinatario_id  │
│ descripcion_paquete  │
│ peso_kg              │
│ fecha_creacion       │
│ fecha_est_entrega    │
│ codigo_sede_registro │
│ creado_por_usuario   │
│ updated_at           │
└──────┬───────────────┘
       │
       ├─────────────────────┐
       │                     │
    FK: remitente_id    FK: destinatario_id
       │                     │
       ▼                     ▼
┌────────────────┐    ┌────────────────┐
│   REMITENTES   │    │ DESTINATARIOS  │
├────────────────┤    ├────────────────┤
│ PK: id         │    │ PK: id         │
│ nombre_completo│    │ nombre_completo│
│ documento      │    │ documento      │
│ email          │    │ email          │
│ telefono       │    │ telefono       │
│ FK: dir_id     │◄───┤ FK: dir_id     │
└────────┬───────┘    └────────┬───────┘
         │                     │
         │    (1:N)            │
         ├─────────────────────┘
         │
         ▼
┌──────────────────────────┐
│     DIRECCIONES          │
├──────────────────────────┤
│ PK: id                   │
│ linea1                   │
│ linea2 (nullable)        │
│ municipio_codigo_dane    │
│ municipio_nombre         │
│ departamento (Antioquia) │
│ pais (Colombia)          │
│ codigo_postal            │
│ referencias              │
└──────────────────────────┘

       (opcional)
         │
         │
         ▼
┌──────────────────────────┐
│  AUDITORIA_ENVIOS        │
├──────────────────────────┤
│ PK: id                   │
│ FK: envio_id             │
│ accion                   │
│ usuario_id_externo       │
│ valores_anteriores (JSONB)
│ valores_nuevos (JSONB)   │
│ created_at               │
└──────────────────────────┘
```

---

## 2. Matriz de Relaciones

| Entidad A | Cardinalidad | Entidad B | Tipo | Descripción |
|-----------|--------------|-----------|------|-------------|
| ESTADOS_ENVIO | 1:N | ENVIOS | Obligatoria | Un estado puede tener múltiples envíos |
| REMITENTES | 1:N | ENVIOS | Obligatoria | Un remitente puede tener múltiples envíos (potencial) |
| DESTINATARIOS | 1:N | ENVIOS | Obligatoria | Un destinatario puede tener múltiples envíos (potencial) |
| DIRECCIONES | 1:N | REMITENTES | Obligatoria | Una dirección es del remitente |
| DIRECCIONES | 1:N | DESTINATARIOS | Obligatoria | Una dirección es del destinatario |
| ENVIOS | 1:N | AUDITORIA_ENVIOS | Opcional | Un envío puede tener múltiples registros de auditoría |

---

## 3. Atributos por Entidad

### ESTADOS_ENVIO
- **id** (PK, Auto-increment): Identificador único
- **codigo** (UNIQUE, Varchar(30)): CREADO, EN_TRANSITO, EN_SEDE, ENTREGADO, INCIDENCIA
- **nombre** (Varchar(100)): Nombre legible del estado
- **descripcion** (Text): Descripción detallada del estado

### DIRECCIONES
- **id** (PK, Auto-increment): Identificador único
- **linea1** (Varchar(255)): Dirección principal (requerida)
- **linea2** (Varchar(255), NULL): Dirección secundaria (ej: apartamento, apto)
- **municipio_codigo_dane** (Varchar(10), NULL): Código oficial DANE del municipio
- **municipio_nombre** (Varchar(100)): Nombre del municipio para búsqueda/display
- **departamento** (Varchar(50), DEFAULT 'Antioquia'): Departamento fijo
- **pais** (Varchar(50), DEFAULT 'Colombia'): País fijo
- **codigo_postal** (Varchar(10), NULL): Código postal
- **referencias** (Text, NULL): Referencias adicionales (ej: "frente al parque")

### REMITENTES
- **id** (PK, Auto-increment): Identificador único
- **nombre_completo** (Varchar(255)): Nombre del remitente
- **documento** (Varchar(20), NULL): Cédula o número de identificación
- **email** (Varchar(100), NULL): Correo electrónico
- **telefono** (Varchar(20), NULL): Número de teléfono
- **direccion_id** (FK, NOT NULL): Referencia a DIRECCIONES

### DESTINATARIOS
- **id** (PK, Auto-increment): Identificador único
- **nombre_completo** (Varchar(255)): Nombre del destinatario
- **documento** (Varchar(20), NULL): Cédula o número de identificación
- **email** (Varchar(100), NULL): Correo electrónico
- **telefono** (Varchar(20), NULL): Número de teléfono
- **direccion_id** (FK, NOT NULL): Referencia a DIRECCIONES

### ENVIOS
- **id** (PK, Auto-increment): Identificador único
- **numero_guia** (Varchar(50), UNIQUE): Guía única del envío
- **estado_envio_id** (FK, NOT NULL): Referencia a ESTADOS_ENVIO
- **remitente_id** (FK, NOT NULL): Referencia a REMITENTES
- **destinatario_id** (FK, NOT NULL): Referencia a DESTINATARIOS
- **descripcion_paquete** (Text, NULL): Descripción del contenido
- **peso_kg** (Decimal(10,2), NULL): Peso del paquete
- **fecha_creacion** (TIMESTAMP): Fecha de creación del envío
- **fecha_estimada_entrega** (Date, NULL): Fecha estimada de entrega
- **codigo_sede_registro** (Varchar(20), NULL): Código de sede (validar con tabla Seguimiento)
- **creado_por_usuario_id_externo** (UUID, NULL): ID externo del usuario que creó el envío
- **updated_at** (TIMESTAMP): Última actualización

### AUDITORIA_ENVIOS
- **id** (PK, Auto-increment): Identificador único
- **envio_id** (FK, NOT NULL): Referencia a ENVIOS
- **accion** (Varchar(50)): CREATE, UPDATE, DELETE, etc.
- **usuario_id_externo** (UUID, NULL): ID externo del usuario que realizó la acción
- **valores_anteriores** (JSONB, NULL): Estado anterior en formato JSON
- **valores_nuevos** (JSONB, NULL): Nuevo estado en formato JSON
- **created_at** (TIMESTAMP): Fecha de la acción de auditoría

---

## 4. Restricciones de Integridad

### Claves Primarias (PK)
- `ESTADOS_ENVIO.id`
- `DIRECCIONES.id`
- `REMITENTES.id`
- `DESTINATARIOS.id`
- `ENVIOS.id`
- `AUDITORIA_ENVIOS.id`

### Claves Únicas (UNIQUE)
- `ESTADOS_ENVIO.codigo` (ej: CREADO, EN_TRANSITO)
- `ENVIOS.numero_guia`

### Claves Foráneas (FK)
- `ENVIOS.estado_envio_id` → `ESTADOS_ENVIO.id` (CASCADE DELETE)
- `ENVIOS.remitente_id` → `REMITENTES.id` (RESTRICT DELETE)
- `ENVIOS.destinatario_id` → `DESTINATARIOS.id` (RESTRICT DELETE)
- `REMITENTES.direccion_id` → `DIRECCIONES.id` (SET NULL o RESTRICT)
- `DESTINATARIOS.direccion_id` → `DIRECCIONES.id` (SET NULL o RESTRICT)
- `AUDITORIA_ENVIOS.envio_id` → `ENVIOS.id` (CASCADE DELETE)

### Restricciones de Dominio
- `ENVIOS.peso_kg` > 0 (si no es NULL)
- `DIRECCIONES.departamento` = 'Antioquia'
- `DIRECCIONES.pais` = 'Colombia'
- `REMITENTES.email` válido (si no es NULL)
- `DESTINATARIOS.email` válido (si no es NULL)

---

## 5. Notas de Diseño

### Normalización
- **Forma Normal de Boyce-Codd (BCNF)**: Todas las entidades están en BCNF
- **Sin redundancia**: Las direcciones se normalizan en tabla separada para evitar duplicación
- **Flexibilidad**: Se permite NULL en campos opcionales para adaptarse a diferentes escenarios

### Consideraciones de Datos
1. **Direcciones**: `municipio_codigo_dane` se valida contra tabla externa de municipios (sin FK)
2. **Auditoria**: JSONB permite rastrear cambios complejos de atributos
3. **Estados**: Predefinidos y limitados (CREADO, EN_TRANSITO, EN_SEDE, ENTREGADO, INCIDENCIA)
4. **Usuarios**: `creado_por_usuario_id_externo` es UUID del sistema Auth (sin FK a BDs)

### Escalabilidad
- Índices recomendados:
  - `ENVIOS.numero_guia` (acceso directo)
  - `ENVIOS.estado_envio_id` (filtrado por estado)
  - `REMITENTES.documento` (búsqueda de remitente)
  - `DESTINATARIOS.documento` (búsqueda de destinatario)
  - `AUDITORIA_ENVIOS.envio_id` (auditoría por envío)
  - `AUDITORIA_ENVIOS.created_at` (rango temporal)

---

## 6. Mapeo a Backend (Spring Boot)

| Tabla | Entidad JPA | DTO | Controlador |
|-------|-------------|-----|-------------|
| ESTADOS_ENVIO | ShipmentState | - | - |
| DIRECCIONES | Address | AddressRequest/Response | EnvioController |
| REMITENTES | Sender/Party | PartyRequest/Response | EnvioController |
| DESTINATARIOS | Recipient/Party | PartyRequest/Response | EnvioController |
| ENVIOS | Shipment | CreateEnvioRequest/ShipmentResponse | EnvioController |
| AUDITORIA_ENVIOS | ShipmentAudit | - | - |

---

## 7. Casos de Uso desde el Modelo

### UC1: Registrar Envío
1. Crear REMITENTE con DIRECCIÓN
2. Crear DESTINATARIO con DIRECCIÓN
3. Crear ENVIO en estado CREADO
4. Crear registro en AUDITORIA_ENVIOS

### UC2: Cambiar Estado
1. Actualizar `ENVIOS.estado_envio_id`
2. Registrar cambio en AUDITORIA_ENVIOS con valores_anteriores y valores_nuevos

### UC3: Buscar Envío
1. Query sobre `ENVIOS.numero_guia`
2. JOIN con REMITENTES, DESTINATARIOS, DIRECCIONES

### UC4: Historial de Auditoría
1. Query sobre `AUDITORIA_ENVIOS` filtrando por `envio_id`
2. Mostrar cambios en orden temporal (created_at)

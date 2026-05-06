# Presentación de los Componentes de Base de Datos del Proyecto `logistica-notificaciones`

**Equipo:** Grupo de estudiantes — Curso: Bases de Datos y Laboratorio

**Profesor:** [Nombre del profesor]

**Fecha:** 06 de mayo de 2026

---

## Objetivo

- **Propósito:** Describir de forma clara y ordenada todos los elementos y componentes de la base de datos empleados en el proyecto `logistica-notificaciones`, explicando su función, relaciones, índices, datos semilla y configuración de conexión.

## Resumen ejecutivo

- **Tecnología principal:** PostgreSQL (DB relacional), uso de tipos `JSONB` para payloads y `UUID` para claves primarias.
- **Migraciones:** Administradas con Flyway mediante scripts en `classpath:db/migration`.
- **Archivos relevantes:** Migraciones y configuración:
  - [src/main/resources/db/migration/V1__notificaciones_schema.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V1__notificaciones_schema.sql#L1-L200)
  - [src/main/resources/db/migration/V2__seed_plantillas.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V2__seed_plantillas.sql#L1-L200)
  - [src/main/resources/db/migration/V3__preferencias_usuario_unique.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V3__preferencias_usuario_unique.sql#L1-L200)
  - [src/main/resources/db/migration/V4__seed_plantilla_auth_login.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V4__seed_plantilla_auth_login.sql#L1-L200)
  - [src/main/resources/application.properties](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/application.properties#L1-L60)

## Flujo de migraciones

- **Orden de ejecución (Flyway):**
  1. `V1__notificaciones_schema.sql` — crea tablas e índices.
  2. `V2__seed_plantillas.sql` — inserta plantillas iniciales.
  3. `V3__preferencias_usuario_unique.sql` — índice único condicional para preferencias.
  4. `V4__seed_plantilla_auth_login.sql` — inserta plantilla adicional (`AUTH_LOGIN`).
- **Ubicación y activación:** Flyway activado en `spring.flyway.enabled=true` y `spring.flyway.locations=classpath:db/migration`.

## Esquema: tablas principales y su propósito

### `plantilla_notificacion`
- **Función:** Almacena plantillas reutilizables para notificaciones (cuerpo, asunto, código).
- **Columnas clave:** `id` (UUID PK, `gen_random_uuid()`), `codigo` (UNIQUE), `asunto`, `cuerpo`, `activo`, `updated_at`.
- **Observación:** Contiene plantillas semilla insertadas en V2/V4.

Ver: [V1__notificaciones_schema.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V1__notificaciones_schema.sql#L1-L30) y [V2__seed_plantillas.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V2__seed_plantillas.sql#L1-L40).

### `notificaciones`
- **Función:** Registra cada notificación enviada o en cola.
- **Columnas clave:** `id` (UUID PK), `canal` (p. ej. EMAIL, PUSH), `destinatario`, `asunto`, `cuerpo`, `plantilla_id` (FK → `plantilla_notificacion.id`), `estado`, `numero_guia`, `payload_evento` (`JSONB`), `proveedor_id_mensaje`, `error_mensaje`, `created_at`, `sent_at`.
- **Relaciones:** FK con `plantilla_notificacion`.
- **Índices:** `idx_notif_estado`, `idx_notif_guia`, `idx_notif_created`.

Ver: [V1__notificaciones_schema.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V1__notificaciones_schema.sql#L8-L40).

### `preferencias_notificaciones`
- **Función:** Guarda preferencias por usuario (qué eventos quiere recibir y por qué canal).
- **Columnas clave:** `id` (UUID PK), `usuario_id_externo` (UUID opcional), `email`, `codigo_evento`, `canal`, `activo`, `updated_at`.
- **Restricciones/índices:** `UNIQUE (email, codigo_evento, canal)` y el índice condicional `uq_pref_usuario_evento_canal` cuando `usuario_id_externo IS NOT NULL`.

Ver: [V1__notificaciones_schema.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V1__notificaciones_schema.sql#L40-L70) y [V3__preferencias_usuario_unique.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V3__preferencias_usuario_unique.sql#L1-L20).

### `consumo_eventos`
- **Función:** Registro de consumo de eventos entrantes (para idempotencia y seguimiento del offset en Kafka).
- **Columnas clave:** `id` (UUID PK), `idempotency_key` (UNIQUE), `topic`, `partition_id`, `offset_val`, `procesado_en`.
- **Uso:** Evita procesar dos veces el mismo evento; almacena partición/offset.

Ver: [V1__notificaciones_schema.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V1__notificaciones_schema.sql#L70-L95).

### `log_auditoria_notificaciones`
- **Función:** Historial/auditoría de acciones relacionadas con `notificaciones`.
- **Columnas clave:** `id` (UUID PK), `notificacion_id` (FK → `notificaciones.id` con `ON DELETE CASCADE`), `nivel`, `mensaje`, `metadata` (`JSONB`), `created_at`.
- **Uso:** Trazabilidad y diagnóstico; borrado en cascada al eliminar notificaciones.

Ver: [V1__notificaciones_schema.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V1__notificaciones_schema.sql#L95-L120).

## Índices y restricciones importantes

- **Índices de rendimiento:**
  - `idx_notif_estado` en `notificaciones(estado)`
  - `idx_notif_guia` en `notificaciones(numero_guia)`
  - `idx_notif_created` en `notificaciones(created_at)`
- **Unicidad y consistencia:**
  - `plantilla_notificacion.codigo` → UNIQUE
  - `preferencias_notificaciones` → `UNIQUE (email, codigo_evento, canal)` + índice condicional por `usuario_id_externo`
  - `consumo_eventos.idempotency_key` → UNIQUE

## Tipos especiales y consideraciones

- **UUIDs:** Uso de `DEFAULT gen_random_uuid()` — requiere extensión adecuada (`pgcrypto` o `uuid-ossp`).
- **JSONB:** `payload_evento` y `metadata` usan `JSONB` para flexibilidad; considerar índices GIN si se consultan.
- **ON DELETE CASCADE:** Aplicado en `log_auditoria_notificaciones`.

## Datos semilla

- **Plantillas iniciales:** Insertadas por `V2__seed_plantillas.sql` (`SHIPMENT_CREATED`, `TRACKING_EVENT_REGISTERED`, `GENERICA`) y `V4__seed_plantilla_auth_login.sql` (`AUTH_LOGIN`).
- **Idempotencia en seeds:** `ON CONFLICT (codigo) DO NOTHING` para evitar duplicados en ejecuciones repetidas.

Ver: [V2__seed_plantillas.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V2__seed_plantillas.sql#L1-L40) y [V4__seed_plantilla_auth_login.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V4__seed_plantilla_auth_login.sql#L1-L40).

## Configuración de conexión y comportamiento JPA

- **Datasource (Postgres):**
  - `spring.datasource.url` por defecto `jdbc:postgresql://localhost:5432/logistica_notificaciones`.
  - Usuario/clave configurables por variables de entorno.
  - Ver: [src/main/resources/application.properties](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/application.properties#L1-L20).
- **JPA / Flyway:**
  - `spring.jpa.hibernate.ddl-auto=validate` — la app valida esquema contra entidades JPA.
  - `spring.flyway.enabled=true` — Flyway aplica migraciones.

## Patrones de diseño y operacionales

- **Idempotencia y procesamiento de eventos:** `consumo_eventos` evita duplicados.
- **Plantillas y preferencias:** Separación que permite reutilización y respeto a preferencias de usuario.
- **Auditoría:** `log_auditoria_notificaciones` para debugging y trazabilidad.

## Recomendaciones y notas para el profesor

- **Extensiones y permisos:** Verificar `gen_random_uuid()`; usar `pgcrypto` o `uuid-ossp` según sea necesario.
- **Índices adicionales:** Considerar índices GIN sobre campos `JSONB` si se consultan frecuentemente.
- **Manejo de retención:** Implementar limpieza/archivado para tablas con alto volumen (`notificaciones`, `log_auditoria_notificaciones`).
- **Migraciones y pruebas:** Probar migraciones en entorno de integración.

## Anexo — ubicación de archivos

- [src/main/resources/db/migration/V1__notificaciones_schema.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V1__notificaciones_schema.sql#L1-L200)
- [src/main/resources/db/migration/V2__seed_plantillas.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V2__seed_plantillas.sql#L1-L200)
- [src/main/resources/db/migration/V3__preferencias_usuario_unique.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V3__preferencias_usuario_unique.sql#L1-L200)
- [src/main/resources/db/migration/V4__seed_plantilla_auth_login.sql](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/db/migration/V4__seed_plantilla_auth_login.sql#L1-L200)
- [src/main/resources/application.properties](https://github.com/juancollante/entregablesBaseDeDatos/blob/main/logistica_notificaciones/src/main/resources/application.properties#L1-L60)

---

¿Desean que convierta este Markdown a PDF o a una presentación PowerPoint?
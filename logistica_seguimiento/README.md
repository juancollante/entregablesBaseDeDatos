# logistica-seguimiento

Microservicio de **seguimiento**. Registra eventos logísticos por guía, consulta historial y publica eventos al **Event Bus (Kafka)** para que `logistica-notificaciones` notifique en tiempo real.

## Variables de entorno

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET` (**obligatorio**, igual al de `logistica-auth`)
- `KAFKA_BOOTSTRAP_SERVERS` (ej. `localhost:9092`)
- `KAFKA_TOPIC` (default `logistica.eventbus`)

## API

- `POST /api/tracking/eventos` (requiere `ROLE_OPERADOR` o `ROLE_ADMIN`)
- `GET /api/tracking/historial/{numeroGuia}` (público)

## Docker local

```bash
docker compose up --build
```

## Esquema en Render

Con la base `logistica_seguimiento` vacía, al desplegar el servicio **Flyway** crea tablas y seeds (`db/migration`).

Si necesitas aplicar el esquema a mano (sin arrancar el JAR), usa el script idempotente del repo:

`sistema-tracking-logistico/scripts/bootstrap-logistica_seguimiento.sql`

## Kafka

Publica evento:

`tracking.event.registered`

con payload mínimo:

```json
{
  "numeroGuia": "ANT-...",
  "tipoEventoCodigo": "RECIBIDO_SEDE",
  "sedeCodigo": "SEDE-MDE-01",
  "fechaHora": "2026-04-25T00:00:00Z",
  "operadorIdExterno": "uuid"
}
```


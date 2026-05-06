# logistica-notificaciones

Microservicio de **notificaciones** del sistema de tracking logístico. Consume eventos desde **Kafka** (Event Bus) y emite notificaciones en **tiempo real** al frontend vía **WebSocket (STOMP + SockJS)**. Opcionalmente envía email si se configura `spring.mail.*`.

## Esquema en Render

Con la base `logistica_notificaciones` vacía, al desplegar el servicio **Flyway** crea tablas y plantillas (`db/migration`).

Script SQL idempotente (manual / sin Flyway): `sistema-tracking-logistico/scripts/bootstrap-logistica_notificaciones.sql`

## Variables de entorno (local / Render)

- `PORT` — por defecto en `application.properties` el servicio usa **8796** (diagrama EAV06 / Event Push UI).
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `KAFKA_BOOTSTRAP_SERVERS` (ej. `localhost:9092`)
- `KAFKA_TOPIC` (default `logistica.eventbus`)
- `KAFKA_CONSUMER_GROUP` (default `logistica-notificaciones`)
- `WS_ALLOWED_ORIGINS` (default `*`)

Email (opcional):

- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`

## WebSocket

- Endpoint: `/ws` (SockJS)
- Topic broadcast: `/topic/notificaciones`

## Contrato de evento (Kafka)

El consumidor espera mensajes JSON con el shape:

```json
{
  "id": "uuid-o-string",
  "type": "shipment.created | tracking.event.registered | auth.login",
  "occurredAt": "2026-04-25T00:00:00Z",
  "payload": {
    "numeroGuia": "ANT-...",
    "destinatarioEmail": "cliente@correo.com"
  }
}
```


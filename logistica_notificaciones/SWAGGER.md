# OpenAPI / Swagger — logistica-notificaciones

Documentación con **springdoc-openapi** y **Swagger UI**. La API REST documentada es **mínima** (salud); Kafka, WebSocket y correo no aparecen como operaciones REST en el spec.

## Rutas (cualquier entorno)

| Recurso | Ruta relativa |
|---------|----------------|
| **Swagger UI** | `/swagger-ui/index.html` |
| **OpenAPI JSON** | `/v3/api-docs` |

**Producción / staging:** `https://<URL pública de notificaciones>/swagger-ui/index.html`.

Servidor en el spec: **`/`** por defecto. Opcional: `openapi.server.url=https://<tu-notificaciones>`.

## Autenticación

Los endpoints REST documentados no usan JWT en el spec.

## APIs documentadas (tags)

| Tag | Ruta | Descripción breve |
|-----|------|-------------------|
| Sistema | `GET /healthz` | Salud del proceso |

## Desactivar documentación

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

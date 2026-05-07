# OpenAPI / Swagger — logistica-seguimiento

Documentación con **springdoc-openapi** y **Swagger UI** en el **mismo origen** que la API desplegada.

## Rutas (cualquier entorno)

| Recurso | Ruta relativa |
|---------|----------------|
| **Swagger UI** | `/swagger-ui/index.html` |
| **OpenAPI JSON** | `/v3/api-docs` |

**Producción / staging:** `https://<URL pública de seguimiento>/swagger-ui/index.html`.

Servidor en el spec: **`/`** por defecto. Opcional: `openapi.server.url=https://<tu-seguimiento>`.

## Autenticación

Tokens desde la URL pública de **logistica-auth**. **Authorize** → *access token* sin `Bearer `.

- **Con JWT:** `GET /api/tracking/sedes`, `POST /api/tracking/eventos`.
- **Sin JWT:** `GET /api/tracking/historial/{numeroGuia}`.

## APIs documentadas (tags)

| Tag | Base | Descripción breve |
|-----|------|-------------------|
| Seguimiento | `/api/tracking` | Sedes, eventos (JWT), historial por guía (público) |

## Desactivar documentación

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

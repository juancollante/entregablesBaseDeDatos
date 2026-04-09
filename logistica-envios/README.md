# logistica-envios

Microservicio de **envíos** del sistema de tracking logístico (ámbito Antioquia, Colombia). Gestiona el alta de envíos (remitente, destinatario, direcciones, número de guía, estado inicial **CREADO**) y la consulta por **número de guía**; persiste en **PostgreSQL** y versiona el esquema con **Flyway**. Valida el **JWT de access (HS256)** que emite **logistica-auth** (mismo secreto; este servicio no emite tokens).

**Arquitectura:** hexagonal — `domain`, `application`, adaptadores `adapter.in.web` y `adapter.out.*` (persistencia, seguridad JWT, notificación por log / extensible a Kafka).

**Ubicación en el monorepo:** el código de este servicio está en la carpeta `sistema-tracking-logistico/logistica-envios` (raíz del módulo Maven de envíos).

---

## Contenido del repositorio

| Elemento | Descripción |
|----------|-------------|
| `src/main/java/.../domain` | Modelos, excepciones, puertos entrantes y salientes |
| `src/main/java/.../application` | Casos de uso (`ShipmentApplicationService`) |
| `src/main/java/.../adapter.in.web` | REST, DTOs, manejo de excepciones, seguridad HTTP |
| `src/main/java/.../adapter.in.web.security` | `ApiSecurityConfig`, filtro JWT, principal de usuario |
| `src/main/java/.../adapter.out.persistence` | JPA, repositorios, adaptador de persistencia, generador de guía `ANT-` + hex |
| `src/main/java/.../adapter.out.security` | Propiedades JWT y clave de firma alineadas con auth |
| `src/main/java/.../adapter.out.messaging` | Notificación al crear envío (log; sustituible por Kafka) |
| `src/main/resources/db/migration` | Scripts Flyway |
| `Dockerfile` | Imagen de ejecución (build Maven + JRE 21) |
| `docker-compose.yml` | Aplicación + PostgreSQL (puerto host **5433** para no chocar con otro Postgres en 5432) |
| `.github/workflows/ci.yml` | Pipeline: `./mvnw verify` en rama `main` o `master` |

---

## Requisitos de entorno

- **JDK 21**
- **PostgreSQL** (local o el contenedor de `docker-compose.yml`)
- Opcional: **Docker** y Docker Compose
- Para probar la API con JWT: **logistica-auth** en ejecución (u otro emisor que use el **mismo** `app.jwt.secret`)

---

## Configuración

Valores por defecto en `src/main/resources/application.properties`:

- Conexión JDBC, usuario y contraseña de PostgreSQL
- `app.jwt.secret` — debe ser el mismo que en **logistica-auth** (HS256)
- `server.port=${PORT:8081}` — en plataformas que definen la variable `PORT` (p. ej. Render), el servicio escucha ese puerto; en local suele ser `8081`

Las propiedades de Spring Boot admiten **sobreescritura por variables de entorno** (`SPRING_DATASOURCE_*`, `APP_JWT_SECRET`, etc.). Los secretos de entornos productivos deben configurarse en el panel del proveedor de nube, no en archivos versionados.

---

## Ejecución con Maven

Desde la raíz del módulo `logistica-envios`:

```bash
./mvnw spring-boot:run
```

API base: `http://localhost:8081`.

---

## Ejecución con Docker Compose

Desde la raíz del módulo:

```bash
docker compose up --build
```

- API: `http://localhost:8081`
- PostgreSQL: puerto en host **5433** (mapeado al 5432 del contenedor; evita choque con otro Postgres local en 5432)

Para definir un secreto JWT en el arranque (debe coincidir con **logistica-auth** si validas tokens entre ambos):

```bash
APP_JWT_SECRET="$(openssl rand -hex 32)" docker compose up --build
```

---

## Imagen Docker aislada

```bash
docker build -t logistica-envios:local .
```

El contenedor requiere variables `SPRING_DATASOURCE_*` (y `APP_JWT_SECRET` en producción) apuntando a una instancia PostgreSQL accesible; el secreto JWT debe coincidir con **logistica-auth** si usas sus tokens.

---

## Integración continua

El workflow **CI logistica-envios** (`.github/workflows/ci.yml`) ejecuta `./mvnw verify` en cada push o pull request hacia `main` o `master`.

---

## Despliegue en la nube (ej. Render)

Patrón habitual: **Web Service** con build desde el `Dockerfile` del repositorio y **PostgreSQL administrado** como recurso aparte. En el panel del servicio web se configuran variables de entorno, por ejemplo:

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET` (mismo valor que en **logistica-auth**)
- `PORT` la suele inyectar el proveedor automáticamente

La URL pública del servicio aparece en el panel una vez desplegado. El archivo `docker-compose.yml` describe el stack para desarrollo local; el despliegue en PaaS suele combinar imagen Docker + base de datos del proveedor mediante esas variables.

---

## API HTTP

| Método | Ruta | Autenticación |
|--------|------|----------------|
| GET | `/actuator/health` | No |
| POST | `/api/envios` | `Authorization: Bearer <access_token>` — roles **ADMIN** u **OPERADOR** |
| GET | `/api/envios/public/{numeroGuia}` | No — resumen sin datos personales (estado, fechas, municipios origen/destino) |
| GET | `/api/envios/{numeroGuia}` | Sí — `Bearer <access_token>`; detalle completo (cualquier rol autenticado) |

Para **POST** y **GET** del detalle completo, el filtro JWT comprueba firma HS256 y el claim `typ` = `access`. Errores habituales: `401`/`403` en rutas protegidas, `404` guía inexistente, `400` validación.

### Flujo con auth (crear envío)

1. `POST` login en **logistica-auth** y copiar `accessToken`.
2. Llamar a **POST /api/envios** con `Authorization: Bearer <accessToken>`.

### Ejemplos con `curl`

Obtener token (auth en 8080):

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@logistica.local","password":"password"}'
```

Crear envío (sustituir `<access_token>`; el cuerpo debe cumplir validación de bean y reglas de negocio — ver DTOs en `adapter.in.web.dto`):

```bash
curl -s -X POST http://localhost:8081/api/envios \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{"remitente":{"nombreCompleto":"Ana López","documento":"123","email":"a@b.com","telefono":"300","direccion":{"linea1":"Calle 1","municipioCodigoDane":"05001","municipioNombre":"Medellín"}},"destinatario":{"nombreCompleto":"Luis Gómez","documento":"456","email":"c@d.com","telefono":"310","direccion":{"linea1":"Carrera 2","municipioCodigoDane":"05088","municipioNombre":"Bello"}},"descripcionPaquete":"Docs","pesoKg":0.5,"fechaEstimadaEntrega":"2026-04-15","codigoSedeRegistro":"SEDE-MDE-01"}'
```

Resumen público por guía (sin token):

```bash
curl -s "http://localhost:8081/api/envios/public/ANT-XXXXXXXXXXXX"
```

Detalle completo (con token):

```bash
curl -s "http://localhost:8081/api/envios/ANT-XXXXXXXXXXXX" \
  -H "Authorization: Bearer <access_token>"
```

### Claims del access token

Los mismos que documenta **logistica-auth**: entre otros `sub`, `typ` = `access`, `email`, `role` (en Spring Security como `ROLE_<código>`), y `sede` cuando aplica.

---

## Notificación de creación

Al crear un envío se invoca el puerto `ShipmentCreatedNotifierPort`; la implementación actual registra en log y deja el punto listo para un productor **Kafka** cuando se cablee.

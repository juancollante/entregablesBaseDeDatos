# logistica-auth

Microservicio de **autenticación** del sistema de tracking logístico. Expone login, renovación de tokens y consulta de perfil; persiste identidad y sesiones en **PostgreSQL**; firma tokens con **JWT (HS256)** y almacena contraseñas con **BCrypt**. El esquema de base de datos se versiona con **Flyway**.

**Arquitectura:** hexagonal — `domain` (reglas y contratos), `application` (casos de uso), adaptadores de entrada (`adapter.in.web`) y de salida (`adapter.out.persistence`, `adapter.out.security`).

---

## Contenido del repositorio

| Elemento | Descripción |
|----------|-------------|
| `src/main/java/.../domain` | Modelos, excepciones de dominio, puertos entrantes y salientes |
| `src/main/java/.../application` | Servicio de autenticación (`AuthenticateUserService`) |
| `src/main/java/.../adapter.in.web` | REST, DTOs, seguridad HTTP y filtro JWT |
| `src/main/java/.../adapter.out.persistence` | Entidades JPA, repositorios y adaptadores de acceso a datos |
| `src/main/java/.../adapter.out.security` | Generación y validación de JWT, BCrypt, refresh token |
| `src/main/resources/db/migration` | Scripts Flyway (esquema y datos iniciales) |
| `Dockerfile` | Imagen de ejecución (build Maven + JRE 21) |
| `docker-compose.yml` | Orquestación local: aplicación + PostgreSQL |
| `.github/workflows/ci.yml` | Pipeline: `./mvnw verify` en rama `main` o `master` |

---

## Requisitos de entorno

- **JDK 21**
- **PostgreSQL** (instalación local o el contenedor definido en `docker-compose.yml`)
- Opcional: **Docker** y Docker Compose para levantar el stack completo

---

## Configuración

Valores por defecto en `src/main/resources/application.properties`:

- Conexión JDBC, usuario y contraseña de PostgreSQL
- `app.jwt.secret`, vigencia de access y refresh (`app.jwt.access-token-minutes`, `app.jwt.refresh-token-days`)
- `server.port=${PORT:8080}` — en plataformas que definen la variable `PORT` (p. ej. Render), el servicio escucha ese puerto; en local suele ser `8080`

Las propiedades de Spring Boot admiten **sobreescritura por variables de entorno** (`SPRING_DATASOURCE_*`, `APP_JWT_SECRET`, etc.). Los secretos de entornos productivos deben configurarse en el panel del proveedor de nube, no en archivos versionados.

---

## Base de datos y migraciones

- Al iniciar, **Flyway** ejecuta los scripts en `src/main/resources/db/migration/`.
- Los datos iniciales incluyen usuarios de desarrollo (misma contraseña `password`): `admin@logistica.local`, `operador@logistica.local` (sede `SEDE-MDE-01`), `cliente@logistica.local` (migraciones `V2` y `V3`).
- En la tabla `refresh_token`, el campo `token_hash` almacena el **SHA-256 en hexadecimal** del JWT de refresh. Las contraseñas de usuario siguen el hash **BCrypt**.

Para desarrollo sin Docker, hay que crear previamente la base y el usuario PostgreSQL coherentes con `application.properties` (o ajustar el archivo).

---

## Ejecución con Maven

```bash
./mvnw spring-boot:run
```

API base: `http://localhost:8080`.

---

## Ejecución con Docker Compose

Desde la raíz del módulo:

```bash
docker compose up --build
```

- API: `http://localhost:8080`
- PostgreSQL: `localhost:5432` (credenciales definidas en `docker-compose.yml`)

Para definir un secreto JWT en el arranque:

```bash
APP_JWT_SECRET="$(openssl rand -hex 32)" docker compose up --build
```

## Imagen Docker aislada

```bash
docker build -t logistica-auth:local .
```

El contenedor requiere variables `SPRING_DATASOURCE_*` (y `APP_JWT_SECRET` en producción) apuntando a una instancia PostgreSQL accesible.

---

## Integración continua

El workflow **CI logistica-auth** (`.github/workflows/ci.yml`) ejecuta `./mvnw verify` en cada push o pull request hacia `main` o `master`.

---

## Despliegue en la nube (ej. Render)

Patrón habitual: **Web Service** con build desde el `Dockerfile` del repositorio y **PostgreSQL administrado** como recurso aparte. En el panel del servicio web se configuran variables de entorno, por ejemplo:

- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET` (valor aleatorio largo)
- `PORT` la suele inyectar el proveedor automáticamente

La URL pública del servicio aparece en el panel una vez desplegado. El archivo `docker-compose.yml` describe el stack para desarrollo local; el despliegue en PaaS suele combinar imagen Docker + base de datos del proveedor mediante esas variables.

---

## API HTTP

| Método | Ruta | Autenticación |
|--------|------|----------------|
| POST | `/api/auth/login` | No |
| POST | `/api/auth/refresh` | No |
| GET | `/api/auth/me` | Cabecera `Authorization: Bearer <access_token>` |

### Ejemplos con `curl`

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@logistica.local","password":"password"}'

curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<valor_del_refresh_token>"}'

curl -s http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <access_token>"
```

### Respuesta de login y refresh (JSON)

Incluye `accessToken`, `refreshToken`, `tokenType` (valor `Bearer`) y `expiresIn` (segundos de vida del access token).

### Claims del access token

Incluye entre otros: `sub` (UUID de usuario), `typ` con valor `access`, `email`, `role` (código de rol; en Spring Security se expone como `ROLE_<código>`), y `sede` cuando aplica.

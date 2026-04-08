# Base de datos de Autenticación

Este documento consolida el diagrama entidad-relación y el modelo lógico para el backend de autenticación del proyecto `logistica-auth`.

## Alcance funcional

El backend revisado actualmente soporta:

- autenticación de usuarios
- emisión de access token y refresh token
- consulta del perfil autenticado
- auditoría de eventos de autenticación
- persistencia de usuarios, roles, refresh token y auditoría

Además, el modelo objetivo incluye permisos, módulos, sesiones persistidas y recuperación de contraseña.

## Diagrama entidad-relación

```mermaid
erDiagram
    ROLES ||--o{ USUARIOS : asigna
    ROLES ||--o{ ROLES_PERMISOS : contiene
    PERMISOS ||--o{ ROLES_PERMISOS : vincula
    MODULOS ||--o{ PERMISOS : agrupa
    USUARIOS ||--o{ AUDITORIA_AUTENTICACION : registra
    USUARIOS ||--o{ SESIONES_ACTIVA : mantiene
    USUARIOS ||--o{ REFRESH_TOKEN : emite
    USUARIOS ||--o{ PASSWORD_RESET_TOKEN : solicita

    ROLES {
        uuid id PK
        varchar codigo UK
        varchar nombre
        varchar descripcion
        timestamptz created_at
    }

    USUARIOS {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar nombre
        varchar apellido
        uuid rol_id FK
        varchar codigo_sede_asignada
        boolean activo
        timestamptz created_at
        timestamptz updated_at
    }

    MODULOS {
        uuid id PK
        varchar codigo UK
        varchar nombre
        int orden
        boolean activo
    }

    PERMISOS {
        uuid id PK
        varchar codigo UK
        varchar nombre
        varchar descripcion
        uuid modulo_id FK
        timestamptz created_at
    }

    ROLES_PERMISOS {
        uuid id PK
        uuid rol_id FK
        uuid permiso_id FK
    }

    AUDITORIA_AUTENTICACION {
        uuid id PK
        uuid usuario_id FK_NULL
        varchar tipo_evento
        varchar ip
        varchar user_agent
        varchar detalle
        timestamptz created_at
    }

    SESIONES_ACTIVA {
        uuid id PK
        uuid usuario_id FK
        varchar jti UK
        varchar ip
        varchar user_agent
        timestamptz created_at
        timestamptz expires_at
        boolean revocada
    }

    REFRESH_TOKEN {
        uuid id PK
        uuid usuario_id FK
        varchar token_hash
        timestamptz expires_at
        boolean revocada
        timestamptz created_at
    }

    PASSWORD_RESET_TOKEN {
        uuid id PK
        uuid usuario_id FK
        varchar token_hash
        timestamptz expires_at
        timestamptz usada_en
        timestamptz created_at
    }
```

## Modelo lógico

### 1. `roles`

- **PK:** `id`
- **UK:** `codigo`
- **Atributos:** `nombre`, `descripcion`, `created_at`
- **Relación:** un rol puede asignarse a muchos usuarios y participar en muchos permisos mediante `roles_permisos`

### 2. `usuarios`

- **PK:** `id`
- **FK:** `rol_id` → `roles.id`
- **UK:** `email`
- **Atributos:** `password_hash`, `nombre`, `apellido`, `codigo_sede_asignada`, `activo`, `created_at`, `updated_at`
- **Regla de negocio:** `codigo_sede_asignada` es nullable y no tiene FK a otra base de datos; representa una relación lógica con la BD de Seguimiento cuando el usuario tiene rol `OPERADOR`

### 3. `modulos`

- **PK:** `id`
- **UK:** `codigo`
- **Atributos:** `nombre`, `orden`, `activo`
- **Relación:** un módulo agrupa varios permisos

### 4. `permisos`

- **PK:** `id`
- **FK:** `modulo_id` → `modulos.id`
- **UK:** `codigo`
- **Atributos:** `nombre`, `descripcion`, `created_at`
- **Relación:** un permiso pertenece a un módulo y puede asignarse a muchos roles

### 5. `roles_permisos`

- **PK recomendada:** compuesta por `rol_id + permiso_id`
- **FK:** `rol_id` → `roles.id`
- **FK:** `permiso_id` → `permisos.id`
- **Restricción:** unicidad por pareja `rol_id + permiso_id`
- **Función:** resolver la relación N:M entre roles y permisos

### 6. `auditoria_autenticacion`

- **PK:** `id`
- **FK opcional:** `usuario_id` → `usuarios.id`
- **Atributos:** `tipo_evento`, `ip`, `user_agent`, `detalle`, `created_at`
- **Regla de negocio:** `usuario_id` puede ser nulo para registrar intentos fallidos sin usuario identificado

### 7. `sesiones_activa`

- **PK:** `id`
- **FK:** `usuario_id` → `usuarios.id`
- **UK:** `jti`
- **Atributos:** `ip`, `user_agent`, `created_at`, `expires_at`, `revocada`
- **Función:** control de sesiones persistidas y revocación

### 8. `refresh_token`

- **PK:** `id`
- **FK:** `usuario_id` → `usuarios.id`
- **Atributos:** `token_hash`, `expires_at`, `revocada`, `created_at`
- **Regla de seguridad:** se almacena el hash del token, nunca el token en claro

### 9. `password_reset_token`

- **PK:** `id`
- **FK:** `usuario_id` → `usuarios.id`
- **Atributos:** `token_hash`, `expires_at`, `usada_en`, `created_at`
- **Función:** recuperación de contraseña

## Cardinalidades principales

- `roles` 1 : N `usuarios`
- `roles` 1 : N `roles_permisos`
- `permisos` 1 : N `roles_permisos`
- `modulos` 1 : N `permisos`
- `usuarios` 1 : N `auditoria_autenticacion`
- `usuarios` 1 : N `sesiones_activa`
- `usuarios` 1 : N `refresh_token`
- `usuarios` 1 : N `password_reset_token`

## Alineación con el backend existente

El esquema físico del proyecto ya contempla estas tablas en Flyway:

- `roles`
- `usuarios`
- `auditoria_autenticacion`
- `refresh_token`
- `modulos`
- `permisos`
- `roles_permisos`
- `sesiones_activa`
- `password_reset_token`

El backend actualmente usa de forma directa:

- `usuarios`
- `roles`
- `auditoria_autenticacion`
- `refresh_token`

y expone en el dominio el código de rol y la sede asignada del usuario, lo que encaja con la regla de operador vinculado lógicamente a una sede.

## Observaciones de diseño

- `roles_permisos` puede modelarse con PK compuesta o con un `id` surrogate; en el SQL actual se usa `id` UUID y una restricción única sobre `rol_id + permiso_id`.
- `tipo_evento` en auditoría puede manejar valores como `login_ok`, `login_fail`, `logout`, `refresh`.
- `sesiones_activa` y `password_reset_token` pueden mantenerse aunque no estén aún consumidas por todos los flujos del backend, porque preparan el crecimiento del módulo de autenticación.

## Resumen

El modelo soporta autenticación, autorización por roles y permisos, trazabilidad de eventos y gestión de tokens, manteniendo separación clara entre identidad, autorización y auditoría.

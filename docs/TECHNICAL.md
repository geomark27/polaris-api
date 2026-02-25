# Technical Reference — Polaris ERP API

_Última actualización: 2026-02-22 (Sprint 2 — seeder usuario admin agregado)_

---

## Stack

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.3 |
| ORM | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL 16 |
| Seguridad | Spring Security + BCrypt + JWT (jjwt 0.12.6) |
| Validación | Jakarta Bean Validation |
| Build | Maven (via wrapper `./mvnw`) |
| Contenedores | Docker Compose |
| Code gen | Lombok |

### Dependencias principales (`pom.xml`)

```
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
spring-boot-starter-webmvc
postgresql (runtime)
lombok (compile-time)
spring-boot-devtools (runtime, development only)
jjwt-api 0.12.6
jjwt-impl 0.12.6 (runtime)
jjwt-jackson 0.12.6 (runtime)
```

---

## Estructura del proyecto

```
src/main/java/com/azenticsys/polaris/
├── PolarisApiApplication.java
├── config/
│   ├── SecurityConfig.java            # FilterChain, CORS, AuthProvider, PasswordEncoder
│   ├── JwtService.java                # Generación y validación de tokens
│   ├── JwtAuthenticationFilter.java   # Interceptor: extrae y valida JWT por request
│   ├── DataInitializer.java           # Seeder: crea usuario admin al primer arranque
│   └── OpenApiConfig.java             # Swagger / OpenAPI con Bearer JWT
├── common/
│   └── exception/
│       ├── ApiError.java              # DTO de respuesta de error estandarizada
│       └── GlobalExceptionHandler.java # @RestControllerAdvice
├── auth/                              # Módulo Auth
│   ├── controller/AuthController.java
│   ├── service/AuthService.java
│   ├── service/AuthServiceImpl.java
│   ├── entity/RevokedToken.java        # Blacklist de tokens revocados
│   ├── repository/RevokedTokenRepository.java
│   ├── scheduler/TokenCleanupScheduler.java  # Limpieza diaria de tokens expirados
│   └── dto/
│       ├── LoginRequest.java
│       ├── RefreshTokenRequest.java
│       └── AuthResponse.java
└── user/                              # Módulo Users
    ├── controller/UserController.java
    ├── service/
    │   ├── UserService.java
    │   ├── UserServiceImpl.java
    │   └── UserDetailsServiceImpl.java  # Implementa UserDetailsService para Spring Security
    ├── repository/UserRepository.java
    ├── entity/User.java
    └── dto/
        ├── CreateUserRequest.java
        ├── UpdateUserRequest.java
        └── UserResponse.java
```

### Patrón de arquitectura

Arquitectura en capas por módulo de dominio:

```
Controller  →  Service (interfaz)  →  ServiceImpl  →  Repository  →  Entity (BD)
    ↑                                                                      ↑
  DTOs                                                               @Entity JPA
```

- Cada módulo es autocontenido: controller, service, repo, entity y DTOs viven juntos.
- Las interfaces de servicio desacoplan el controller de la implementación.
- Los DTOs son Java Records (inmutables, sin boilerplate).
- La inyección de dependencias es siempre por constructor (`@RequiredArgsConstructor` de Lombok).

---

## Configuración

### `application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5433}/${DB_NAME:polaris_db}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: ${JPA_DDL_AUTO:update}
    show-sql: ${JPA_SHOW_SQL:false}

server:
  port: ${PORT:8080}

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: ${JWT_EXPIRATION_MS:86400000}
    refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
  seed:
    admin:
      username: ${ADMIN_USERNAME:admin}
      email: ${ADMIN_EMAIL:admin@polaris.local}
      password: ${ADMIN_PASSWORD:Admin1234!}
```

Toda la configuración sensible se inyecta vía variables de entorno. Ver `.env.example` para la lista completa de variables requeridas.

---

## Módulo: Users

### Entidad `User`

| Campo | Tipo Java | Columna BD | Restricciones |
|---|---|---|---|
| `id` | `UUID` | `id` | PK, auto-generado (UUIDv4), no actualizable |
| `username` | `String` | `username` | NOT NULL, UNIQUE, max 50 chars |
| `email` | `String` | `email` | NOT NULL, UNIQUE, max 150 chars |
| `passwordHash` | `String` | `password_hash` | NOT NULL |
| `phoneNumber` | `String` | `phone_number` | nullable, max 20 chars |
| `isActive` | `boolean` | `is_active` | NOT NULL, default `true` |
| `createdAt` | `LocalDateTime` | `created_at` | NOT NULL, no actualizable, seteado en `@PrePersist` |
| `updatedAt` | `LocalDateTime` | `updated_at` | NOT NULL, seteado en `@PrePersist` y `@PreUpdate` |
| `deletedAt` | `LocalDateTime` | `deleted_at` | nullable, soft delete |

### DTOs

**`CreateUserRequest`**
```java
record CreateUserRequest(
    String username,     // @NotBlank, @Size(min=3, max=50)
    String email,        // @NotBlank, @Email, @Size(max=150)
    String password,     // @NotBlank, @Size(min=8)
    String phoneNumber   // @Pattern(...), nullable
)
```

**`UpdateUserRequest`**
```java
record UpdateUserRequest(
    String username,     // @Size(min=3, max=50), nullable
    String phoneNumber   // @Pattern(...), nullable
)
```

**`UserResponse`** (nunca expone `passwordHash` ni `deletedAt`)
```java
record UserResponse(
    UUID id,
    String username,
    String email,
    String phoneNumber,
    boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
)
```

---

## Endpoints

Base URL: `http://localhost:8080`

---

### `POST /api/v1/users`
Crea un nuevo usuario.

**Request body**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "secret123",
  "phoneNumber": "+57 300 123 4567"
}
```

**Response `201 Created`**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "email": "john@example.com",
  "phoneNumber": "+57 300 123 4567",
  "isActive": true,
  "createdAt": "2026-02-22T10:00:00",
  "updatedAt": "2026-02-22T10:00:00"
}
```

**Errores posibles**
- `400` — Validación fallida (campo requerido, formato inválido, contraseña corta)
- `400` — Username o email ya en uso

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "secret123",
    "phoneNumber": "+57 300 123 4567"
  }'
```

---

### `GET /api/v1/users`
Retorna todos los usuarios activos (no eliminados lógicamente).

**Response `200 OK`**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "john@example.com",
    "phoneNumber": "+57 300 123 4567",
    "isActive": true,
    "createdAt": "2026-02-22T10:00:00",
    "updatedAt": "2026-02-22T10:00:00"
  }
]
```

```bash
curl http://localhost:8080/api/v1/users
```

---

### `GET /api/v1/users/{id}`
Retorna un usuario por su UUID.

**Path param:** `id` — UUID del usuario

**Response `200 OK`** — mismo schema que un elemento de `GET /api/v1/users`

**Errores posibles**
- `400` — Usuario no encontrado o eliminado lógicamente

```bash
curl http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000
```

---

### `PATCH /api/v1/users/{id}`
Actualización parcial de un usuario. Solo `username` y `phoneNumber` son modificables.

**Path param:** `id` — UUID del usuario

**Request body** (todos los campos son opcionales)
```json
{
  "username": "john_updated",
  "phoneNumber": "+57 311 999 8888"
}
```

**Response `200 OK`** — usuario actualizado, mismo schema que `UserResponse`

**Errores posibles**
- `400` — Nuevo username ya tomado
- `400` — Usuario no encontrado o eliminado

```bash
curl -X PATCH http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -d '{"username": "john_updated"}'
```

---

### `DELETE /api/v1/users/{id}`
Eliminación lógica. Marca `deleted_at` y pone `is_active = false`. No borra el registro.

**Path param:** `id` — UUID del usuario

**Response `204 No Content`**

**Errores posibles**
- `400` — Usuario no encontrado o ya eliminado

```bash
curl -X DELETE http://localhost:8080/api/v1/users/550e8400-e29b-41d4-a716-446655440000
```

---

## Módulo: Auth

### DTOs

**`LoginRequest`**
```java
record LoginRequest(
    String username,  // @NotBlank
    String password   // @NotBlank
)
```

**`RefreshTokenRequest`**
```java
record RefreshTokenRequest(
    String refreshToken  // @NotBlank
)
```

**`AuthResponse`**
```java
record AuthResponse(
    UUID userId,
    String username,
    String accessToken,
    String refreshToken
)
```

---

### `POST /api/v1/auth/login`
Autentica un usuario y retorna un par de tokens JWT.

**Request body**
```json
{ "username": "john_doe", "password": "secret123" }
```

**Response `200 OK`**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "john_doe",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```

**Errores posibles**
- `400` — Campos requeridos vacíos
- `401` — Credenciales inválidas o cuenta desactivada

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "john_doe", "password": "secret123"}'
```

---

### `POST /api/v1/auth/logout`
Invalida el `accessToken` actual agregándolo a la blacklist en BD. Requiere JWT.

**Headers:** `Authorization: Bearer <accessToken>`

**Response `204 No Content`**

**Errores posibles**
- `401` — Token inválido o ya expirado

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJ..."
```

---

### `POST /api/v1/auth/refresh`
Renueva el par de tokens usando un `refreshToken` vigente.

**Request body**
```json
{ "refreshToken": "eyJ..." }
```

**Response `200 OK`** — mismo schema que `AuthResponse`

**Errores posibles**
- `401` — Token inválido, expirado, o se recibió un accessToken en lugar de refreshToken

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "eyJ..."}'
```

---

## Seguridad

**Estrategia:** Stateless. No hay sesión en el servidor. Cada request debe incluir un `accessToken` válido en el header `Authorization`.

**Rutas públicas:** solo `/api/v1/auth/**`. El resto requiere JWT.

**Flujo de autenticación por request:**
```
Request → JwtAuthenticationFilter
              ↓
    Extrae Bearer token del header
              ↓
    JwtService valida firma y expiración
              ↓
    Verifica que sea un accessToken (no refresh)
              ↓
    Verifica que el jti NO esté en revoked_tokens (blacklist)
              ↓
    Carga UserDetails desde BD
              ↓
    Inyecta Authentication en SecurityContext
              ↓
    Continúa hacia el Controller
```

**Tabla `revoked_tokens`:** almacena el `jti` de tokens invalidados por logout. Un scheduler corre cada medianoche y elimina los registros cuyo `expires_at` ya pasó, manteniendo la tabla limpia.

**CORS** configurado vía bean `CorsConfigurationSource`. Orígenes permitidos definidos en `CORS_ALLOWED_ORIGINS` del `.env`.

---

## Manejo de errores

Todas las respuestas de error siguen el schema `ApiError`:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields are invalid",
  "errors": [
    { "field": "email", "message": "Email must be valid" }
  ],
  "timestamp": "2026-02-22T10:00:00"
}
```

El campo `errors` solo aparece en errores de validación (`400`). En los demás casos es `null` y se omite.

| Excepción | HTTP | `error` |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `Validation Failed` |
| `IllegalArgumentException` | 400 | `Bad Request` |
| `BadCredentialsException` | 401 | `Unauthorized` |
| `AuthorizationDeniedException` | 403 | `Forbidden` |
| `Exception` (genérico) | 500 | `Internal Server Error` |

---

## Seeder / Data Initializer

`DataInitializer` implementa `CommandLineRunner` y se ejecuta una sola vez al arrancar la aplicación.

**Lógica:**
1. Consulta `userRepository.count()`.
2. Si la tabla `users` está vacía, crea el usuario admin con la contraseña encodeada en BCrypt.
3. Si ya existen usuarios, no hace nada (idempotente).

**Credenciales del admin por defecto** (configurables vía `.env`):

| Variable | Valor por defecto |
|---|---|
| `ADMIN_USERNAME` | `admin` |
| `ADMIN_EMAIL` | `admin@polaris.local` |
| `ADMIN_PASSWORD` | `Admin1234!` |

> **Importante:** cambiar `ADMIN_PASSWORD` antes de desplegar en cualquier entorno que no sea local.

---

## Base de datos

PostgreSQL corre en Docker. Para levantarla:

```bash
make docker-up
```

Conexión directa:
```bash
make db-connect
# equivale a: docker compose exec postgres psql -U <DB_USER> -d <DB_NAME>
```

Hibernate gestiona el schema automáticamente con `ddl-auto=update` en desarrollo. En producción se debe cambiar a `validate` y gestionar migraciones manualmente o con Flyway/Liquibase. <!-- TODO: evaluar adopción de Flyway -->

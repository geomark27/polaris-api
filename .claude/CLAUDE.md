# Polaris ERP API

REST API stateless para el sistema ERP multi-tenant Polaris.

**Stack:** Java 21 · Spring Boot 4.0.3 · PostgreSQL 16 · JWT (jjwt 0.12.6) · Lombok · SpringDoc OpenAPI 3.0

**Estado actual:** 74% completo — Multi-tenancy implementado (Sprint 0 completo)

---

## Multi-tenancy (CORE)

**Estrategia:** Schema por tenant en PostgreSQL. Un solo cluster, una DB, schemas separados.

- `landlord` schema: `tenants`, `revoked_tokens` (datos del sistema SaaS)
- `public` schema: tablas template creadas por Hibernate DDL al arrancar
- `t_{slug}` schema: un schema por cada organización registrada (ej: `t_torres_y_torres`)

**Resolución del tenant por request:**
1. `TenantFilter` lee header `X-Tenant-ID` (slug) → busca en `landlord.tenants` → setea `TenantContext`
2. `JwtAuthenticationFilter` lee `tenantSchema` del claim JWT → sobreescribe `TenantContext` (más seguro)
3. `TenantAwareDataSource.getConnection()` ejecuta `SET search_path TO {schema}, landlord, public`
4. Todas las queries del request resuelven contra el schema del tenant

**Slug style:** Como Atlassian — nombre URL-friendly elegido al registrar (ej: `torres-y-torres`, no el RUC).

**Login:**
```bash
curl -X POST /api/v1/auth/login \
  -H "X-Tenant-ID: torres-y-torres" \
  -d '{"username":"admin","password":"Admin1234!"}'
# El JWT devuelto incluye tenantSchema → ya no necesitas el header en requests posteriores
```

---

## Estructura de paquetes

```
com.azenticsys.polaris/
├── PolarisApiApplication.java       # Entry point, @EnableScheduling
├── config/                          # Configuración global
│   ├── SecurityConfig.java          # Spring Security + JWT filter chain + CORS
│   ├── JwtService.java              # Tokens JWT (incluye tenantSchema claim)
│   ├── JwtAuthenticationFilter.java # Valida JWT + setea TenantContext desde claim
│   ├── DataSourceConfig.java        # @Primary DataSource = TenantAwareDataSource
│   ├── DataInitializer.java         # Seeder: crea tenant "polaris" + admin + catalogos
│   ├── OpenApiConfig.java           # Swagger/OpenAPI con Bearer auth
│   └── multitenancy/
│       ├── TenantContext.java       # ThreadLocal: schema name activo
│       ├── TenantAwareDataSource.java # DataSource que hace SET search_path
│       └── TenantFilter.java        # Filter @Order(1): resuelve desde X-Tenant-ID
├── common/
│   └── exception/
│       ├── ApiError.java            # DTO de error estandarizado
│       └── GlobalExceptionHandler.java  # @RestControllerAdvice
├── tenant/                          # Módulo landlord — gestión de organizaciones
│   ├── controller/TenantController.java
│   ├── service/TenantService.java + TenantServiceImpl.java
│   ├── entity/Tenant.java           # @Table(schema="landlord") — slug, schemaName
│   ├── repository/TenantRepository.java
│   └── dto/CreateTenantRequest, TenantResponse
├── auth/                            # Módulo autenticación
│   ├── controller/AuthController.java
│   ├── service/AuthService.java + AuthServiceImpl.java
│   ├── entity/RevokedToken.java     # @Table(schema="landlord") — blacklist global
│   ├── repository/RevokedTokenRepository.java
│   ├── scheduler/TokenCleanupScheduler.java  # Limpieza diaria de tokens expirados
│   └── dto/LoginRequest, RefreshTokenRequest, AuthResponse (incluye tenantSlug)
└── user/                            # Módulo usuarios (schema del tenant)
    ├── controller/UserController.java
    ├── service/UserService.java + UserServiceImpl.java + UserDetailsServiceImpl.java
    ├── entity/User.java             # Sin schema explícito → resuelve vía search_path
    ├── repository/UserRepository.java
    └── dto/CreateUserRequest, UpdateUserRequest, UserResponse
```

### Patrón por módulo

Cada módulo nuevo sigue esta estructura en `com.azenticsys.polaris.{nombre}/`:
- `controller/` → HTTP layer, recibe/devuelve DTOs
- `service/` → Interfaz + implementación con lógica de negocio
- `repository/` → JPA repository
- `entity/` → Entidad JPA (tabla en BD)
- `dto/` → Records Java (inmutables, sin boilerplate)

---

## Endpoints

### Tenants — `/api/v1/tenants` (landlord)
| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/v1/tenants/register` | público | Registrar nueva organización → crea schema PostgreSQL |
| GET | `/api/v1/tenants` | JWT | Listar tenants |
| GET | `/api/v1/tenants/{id}` | JWT | Obtener tenant por UUID |
| DELETE | `/api/v1/tenants/{id}` | JWT | Desactivar tenant |

### Auth — `/api/v1/auth` (requiere header `X-Tenant-ID` en login)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | Login con username/password → accessToken + refreshToken + tenantSlug |
| POST | `/api/v1/auth/refresh` | Renovar accessToken (tenantSchema viene del refreshToken) |
| POST | `/api/v1/auth/logout` | Revocar token (requiere JWT) |

### Users — `/api/v1/users` (requiere JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/users` | Crear usuario → 201 |
| GET | `/api/v1/users` | Listar usuarios activos |
| GET | `/api/v1/users/{id}` | Obtener usuario por UUID |
| PATCH | `/api/v1/users/{id}` | Actualizar (solo username y phoneNumber) |
| DELETE | `/api/v1/users/{id}` | Soft delete → 204 |

---

## Entidades principales

### User
```
id (UUID, PK, auto) | username (UNIQUE) | email (UNIQUE) | password_hash (BCrypt)
phone_number | is_active | created_at | updated_at | deleted_at (soft delete)
```
- Soft delete: `deletedAt` + `isActive = false`
- Solo `username` y `phoneNumber` son editables via PATCH
- Email y password NO son editables por API (pendiente de implementar)

### RevokedToken
```
jti (PK) | revoked_at | expires_at
```
- Blacklist de JWTs revocados al hacer logout
- Limpieza automática diaria (TokenCleanupScheduler, cron: `0 0 0 * * *`)

---

## Seguridad

- **Stateless:** Sin sesiones HTTP, CSRF deshabilitado
- **JWT flow:** `Authorization: Bearer <token>` → JwtAuthenticationFilter → valida firma + expiración + tipo (access) + jti no revocado
- **Tokens:** accessToken (24h) + refreshToken (7 días), configurables en `.env`
- **Rutas públicas:** `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **CORS:** Configurable via `CORS_ALLOWED_ORIGINS` en `.env`
- **Contraseñas:** BCrypt strength 10

---

## Base de datos

- **Motor:** PostgreSQL 16 (Alpine), puerto `5433` (mapeado desde 5432 en container)
- **DDL:** `ddl-auto=update` en desarrollo (Hibernate auto-update)
- **Conexión:** `jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}`
- **Producción:** cambiar a `validate` con Flyway/Liquibase

---

## Comandos de desarrollo

```bash
# Arranque diario
make dev              # docker-up + compile + run (todo en uno)

# Por separado
make docker-up        # Inicia PostgreSQL
make run              # spring-boot:run (dev mode con hot reload)
make compile          # Compilar sin tests

# Tests
make test             # Todos los tests
make test-unit        # Solo unitarios
make test-integration # Solo integración

# Build
make package          # Genera JAR
make ci               # compile + test + package (CI pipeline)

# Base de datos
make db-connect       # psql al contenedor
make db-reset         # Borra volumen y recrea DB

# Generar nuevo módulo
make module name=product  # Crea estructura completa (entity, repo, service, controller, DTOs)

# Git
make push m="mensaje"     # add + commit + push
make sync m="mensaje"     # pull + commit + push
```

---

## Configuración y variables de entorno

Archivo: `.env` (basado en `.env.example`)

| Variable | Default | Descripción |
|----------|---------|-------------|
| `PORT` | 8080 | Puerto del servidor |
| `DB_HOST` | localhost | Host PostgreSQL |
| `DB_PORT` | 5433 | Puerto PostgreSQL |
| `DB_NAME` | polaris_db | Nombre de la base de datos |
| `DB_USER` | — | Usuario BD |
| `DB_PASSWORD` | — | Contraseña BD |
| `JPA_DDL_AUTO` | update | Estrategia DDL de Hibernate |
| `JPA_SHOW_SQL` | false | Mostrar SQL en logs |
| `JWT_SECRET` | — | Secreto JWT (mín. 64 chars) |
| `JWT_EXPIRATION_MS` | 86400000 | Expiración accessToken (24h) |
| `JWT_REFRESH_EXPIRATION_MS` | 604800000 | Expiración refreshToken (7d) |
| `CORS_ALLOWED_ORIGINS` | http://localhost:3000 | Orígenes CORS permitidos |
| `ADMIN_USERNAME` | admin | Usuario admin inicial |
| `ADMIN_EMAIL` | admin@polaris.local | Email admin inicial |
| `ADMIN_PASSWORD` | Admin1234! | Password admin inicial |

**Swagger UI:** http://localhost:8080/swagger-ui.html
**OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## Convenciones del proyecto

- **DTOs:** Java Records (inmutables, sin Lombok)
- **Servicios:** Interfaz + implementación separadas (`UserService` + `UserServiceImpl`)
- **Inyección de dependencias:** Constructor injection via `@RequiredArgsConstructor` (Lombok)
- **UUIDs:** `@UuidGenerator` de Hibernate (UUIDv4 auto-generado)
- **Soft delete:** Siempre `deletedAt` + `isActive`, nunca `DELETE` físico en `users`
- **Errores:** Siempre `ApiError` via `GlobalExceptionHandler`
- **Versionado de API:** `/api/v1/` prefijo en todos los endpoints

---

## Estado del proyecto (Sprints)

| Sprint | Descripción | Estado |
|--------|-------------|--------|
| 1 | Infraestructura base | 100% |
| 2 | CRUD Usuarios | 78% (falta: cambio de password y email) |
| 3 | Autenticación JWT | 100% |
| 4 | Manejo de errores | 100% |
| 5 | Módulos ERP | 0% (pendiente) |
| 6 | Tests | 0% (pendiente) |

**Pendiente Sprint 2:**
- Flujo de cambio de contraseña
- Flujo de cambio de email

**Próximo (Sprint 5):** Módulos ERP (productos, inventario, etc.) — usar `make module name=<modulo>`

---

## Documentación adicional

- `docs/TECHNICAL.md` — Arquitectura, esquemas de BD, ejemplos cURL
- `docs/BUSINESS_LOGIC.md` — Reglas de negocio, flujos, restricciones
- `docs/SPRINT_PLANNING.md` — Estado detallado de cada sprint y tarea

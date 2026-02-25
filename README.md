# Polaris ERP — API

REST API stateless para el sistema ERP multi-tenant Polaris.

**Stack:** Java 21 · Spring Boot 4.0.3 · PostgreSQL 16 · JWT (jjwt 0.12.6) · Lombok · SpringDoc OpenAPI 3.0

---

## Requisitos

- Java 21
- Docker y Docker Compose
- Maven (incluido via `./mvnw`)

---

## Inicio rápido

```bash
# 1. Copiar y configurar variables de entorno
cp .env.example .env

# 2. Setup inicial (solo la primera vez)
make setup

# 3. Arranque diario
make dev
```

Una vez levantado:

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## Comandos principales

| Comando | Descripción |
|---------|-------------|
| `make dev` | Docker + compilar + ejecutar (arranque diario) |
| `make docker-up` | Levantar PostgreSQL |
| `make docker-down` | Detener servicios |
| `make compile` | Compilar sin tests |
| `make test` | Ejecutar todos los tests |
| `make test-unit` | Solo tests unitarios |
| `make test-integration` | Solo tests de integración |
| `make package` | Generar JAR |
| `make ci` | compile + test + package |
| `make db-connect` | Conectar a PostgreSQL via psql |
| `make db-reset` | Borrar y recrear la BD |
| `make module name=product` | Generar estructura de un nuevo módulo |
| `make push m="mensaje"` | git add + commit + push |
| `make help` | Ver todos los comandos disponibles |

---

## Variables de entorno

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

---

## Endpoints

### Auth — `/api/v1/auth` (público)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/login` | Login → accessToken + refreshToken |
| POST | `/api/v1/auth/refresh` | Renovar accessToken |
| POST | `/api/v1/auth/logout` | Revocar token (requiere JWT) |

### Users — `/api/v1/users` (requiere JWT)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/users` | Crear usuario → 201 |
| GET | `/api/v1/users` | Listar usuarios con paginación y filtros |
| GET | `/api/v1/users/{id}` | Obtener usuario por UUID |
| PATCH | `/api/v1/users/{id}` | Actualizar username y/o phoneNumber |
| DELETE | `/api/v1/users/{id}` | Soft delete → 204 |

#### Parámetros de paginación y filtrado (`GET /api/v1/users`)

| Parámetro | Default | Descripción |
|-----------|---------|-------------|
| `page` | 0 | Número de página (0-indexed) |
| `size` | 10 | Registros por página (máx. 200) |
| `sortBy` | createdAt | Campo de ordenamiento |
| `sortDir` | desc | Dirección: `asc` o `desc` |
| `username` | — | Filtro parcial por username |
| `email` | — | Filtro parcial por email |
| `phoneNumber` | — | Filtro parcial por teléfono |

```
GET /api/v1/users?page=0&size=20&username=admin&sortBy=username&sortDir=asc
```

---

## Estructura del proyecto

```
src/main/java/com/azenticsys/polaris/
├── config/          # SecurityConfig, JwtService, DataInitializer, OpenApiConfig
├── common/
│   ├── exception/   # ApiError, GlobalExceptionHandler
│   └── pagination/  # PageResponse<T>, PageQuery (reutilizables)
├── auth/            # Login, refresh, logout, blacklist de tokens
└── user/            # CRUD de usuarios
```

### Patrón por módulo

Cada módulo sigue la estructura: `controller/` · `service/` · `repository/` · `entity/` · `dto/`

Usa `make module name=<nombre>` para generar la estructura automáticamente.

---

## Seguridad

- **Stateless:** sin sesiones HTTP, CSRF deshabilitado
- **JWT:** accessToken (24h) + refreshToken (7d)
- **Logout:** blacklist de JTIs en BD, limpieza automática diaria
- **Contraseñas:** BCrypt strength 10
- **Rutas públicas:** `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`

---

## Documentación adicional

- [`docs/TECHNICAL.md`](docs/TECHNICAL.md) — Arquitectura, esquemas de BD, ejemplos cURL
- [`docs/BUSINESS_LOGIC.md`](docs/BUSINESS_LOGIC.md) — Reglas de negocio, flujos, restricciones
- [`docs/SPRINT_PLANNING.md`](docs/SPRINT_PLANNING.md) — Estado detallado de cada sprint

---

## Estado del proyecto

| Sprint | Descripción | Estado |
|--------|-------------|--------|
| 1 | Infraestructura base | 100% |
| 2 | CRUD Usuarios | 78% |
| 3 | Autenticación JWT | 100% |
| 4 | Manejo de errores | 100% |
| 5 | Módulos ERP | 0% |
| 6 | Tests | 0% |

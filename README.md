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

# 2. Generar JWT_SECRET y pegarlo en .env
make gen-jwt

# 3. Setup inicial (solo la primera vez)
make setup

# 4. Arranque diario
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
| `make package` | Generar JAR |
| `make ci` | compile + test + package |
| `make db-connect` | Conectar a PostgreSQL via psql |
| `make db-reset` | Borrar y recrear la BD |
| `make gen-jwt` | Generar JWT_SECRET seguro para .env |
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
| `JWT_SECRET` | — | Secreto JWT (mín. 64 chars, usar `make gen-jwt`) |
| `JWT_EXPIRATION_MS` | 86400000 | Expiración accessToken (24h) |
| `JWT_REFRESH_EXPIRATION_MS` | 604800000 | Expiración refreshToken (7d) |
| `CORS_ALLOWED_ORIGINS` | http://localhost:3000 | Orígenes CORS permitidos |
| `ADMIN_USERNAME` | admin | Usuario admin inicial |
| `ADMIN_EMAIL` | admin@polaris.local | Email admin inicial |
| `ADMIN_PASSWORD` | Admin1234! | Password admin inicial |

---

## Estado del proyecto

| Sprint | Descripción | Estado |
|--------|-------------|--------|
| 0 | Multi-tenancy (core) | 100% ✅ |
| 1 | Infraestructura base | 100% ✅ |
| 2 | Módulo Users (CRUD + cambio de contraseña) | 100% ✅ |
| 3 | Autenticación JWT | 100% ✅ |
| 4 | Manejo global de errores | 100% ✅ |
| 5 | Catálogos, Productos y Categorías | 100% ✅ |
| 6 | Configuración de empresa | 0% ⏳ |
| 7+ | Clientes, Inventario, Ventas, Compras... | 0% ⏳ |

---

## Documentación

- [`docs/SPRINT_PLANNING.md`](docs/SPRINT_PLANNING.md) — Estado detallado de cada sprint
- [`docs/TECHNICAL.md`](docs/TECHNICAL.md) — Arquitectura, esquemas de BD, ejemplos cURL
- [`docs/BUSINESS_LOGIC.md`](docs/BUSINESS_LOGIC.md) — Reglas de negocio, flujos, restricciones
- [`docs/sprints/`](docs/sprints/) — Guías de implementación por sprint

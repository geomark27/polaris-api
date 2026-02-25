# Sprint Planning — Polaris ERP API

_Última actualización: 2026-02-22 (Sprint 2 completado — seeder usuario admin agregado)_

---

## Progreso general

| Sprint | Descripción | Avance |
|---|---|---|
| Sprint 1 | Infraestructura base | 100% |
| Sprint 2 | Módulo Users (CRUD) | 88% |
| Sprint 3 | Autenticación JWT | 100% |
| Sprint 4 | Manejo global de errores | 100% |
| Sprint 5 | Módulos de negocio ERP | 0% |
| Sprint 6 | Testing | 0% |
| **Total** | | **58%** |

---

## Sprint 1 — Infraestructura base
**Estado: ✅ Completo**

| Tarea | Estado |
|---|---|
| Proyecto Spring Boot inicializado (Java 21, Maven) | ✅ |
| Dependencias base configuradas (JPA, Security, Validation, WebMVC) | ✅ |
| PostgreSQL en Docker Compose | ✅ |
| `application.yaml` con variables de entorno | ✅ |
| `.env.example` y `.env` estructurados | ✅ |
| Makefile con comandos de desarrollo, Docker y Git | ✅ |
| Estructura de módulos definida | ✅ |

**Avance: 7/7 — 100%**

---

## Sprint 2 — Módulo Users (CRUD)
**Estado: 🔄 En progreso**

| Tarea | Estado |
|---|---|
| Entidad `User` con UUID, soft delete y auditoría | ✅ |
| `UserRepository` con queries para soft delete | ✅ |
| DTOs con validaciones (`CreateUserRequest`, `UpdateUserRequest`, `UserResponse`) | ✅ |
| `UserService` (interfaz + implementación) | ✅ |
| `UserController` con endpoints REST | ✅ |
| `SecurityConfig` con `PasswordEncoder` (BCrypt) | ✅ |
| `DataInitializer` — seeder de usuario admin por defecto | ✅ |
| Cambio de contraseña (flujo dedicado con verificación) | ❌ |
| Cambio de email (flujo dedicado con confirmación) | ❌ |

**Avance: 7/9 — 78%**

---

## Sprint 3 — Autenticación JWT
**Estado: ✅ Completo**

| Tarea | Estado |
|---|---|
| Dependencia `jjwt 0.12.6` agregada al `pom.xml` | ✅ |
| `JwtService` (generación y validación de tokens con `jti`) | ✅ |
| `JwtAuthenticationFilter` (interceptor de requests + blacklist check) | ✅ |
| `UserDetailsServiceImpl` (integración con Spring Security) | ✅ |
| Endpoint `POST /api/v1/auth/login` | ✅ |
| Endpoint `POST /api/v1/auth/refresh` | ✅ |
| Proteger rutas en `SecurityConfig` (requiere JWT) | ✅ |
| Endpoint `POST /api/v1/auth/logout` (blacklist en BD) | ✅ |
| `RevokedToken` entity + repository + limpieza automática diaria | ✅ |

**Avance: 9/9 — 100%**

---

## Sprint 4 — Manejo global de errores
**Estado: ✅ Completo**

| Tarea | Estado |
|---|---|
| `GlobalExceptionHandler` con `@RestControllerAdvice` | ✅ |
| Respuesta de error estandarizada (`ApiError`) | ✅ |
| Manejo de `MethodArgumentNotValidException` (validaciones) | ✅ |
| Manejo de excepciones de negocio (`IllegalArgumentException`) | ✅ |
| Manejo de errores de autenticación/autorización (`BadCredentialsException`, `AuthorizationDeniedException`) | ✅ |

**Avance: 5/5 — 100%**

---

## Sprint 5 — Módulos de negocio ERP
**Estado: ⏳ Pendiente**

<!-- TODO: definir módulos específicos del ERP según requerimientos del negocio -->

| Tarea | Estado |
|---|---|
| Definición de módulos ERP requeridos | ❌ |
| Módulo: <!-- TODO: verificar --> | ❌ |
| Módulo: <!-- TODO: verificar --> | ❌ |

**Avance: 0/N — 0%**

---

## Sprint 6 — Testing
**Estado: ⏳ Pendiente**

| Tarea | Estado |
|---|---|
| Tests unitarios para `UserServiceImpl` | ❌ |
| Tests de integración para `UserController` | ❌ |
| Tests unitarios para `JwtService` | ❌ |
| Tests de integración para endpoints de auth | ❌ |
| Configurar base de datos H2 o Testcontainers para tests | ❌ |

**Avance: 0/5 — 0%**

---

## Endpoints — Estado de integración

| Método | Ruta | Implementado | Protegido con JWT | Integrado en cliente |
|---|---|---|---|---|
| POST | `/api/v1/auth/login` | ✅ | N/A (pública) | ❌ |
| POST | `/api/v1/auth/refresh` | ✅ | N/A (pública) | ❌ |
| POST | `/api/v1/auth/logout` | ✅ | ✅ | ❌ |
| POST | `/api/v1/users` | ✅ | ✅ | ❌ |
| GET | `/api/v1/users` | ✅ | ✅ | ❌ |
| GET | `/api/v1/users/{id}` | ✅ | ✅ | ❌ |
| PATCH | `/api/v1/users/{id}` | ✅ | ✅ | ❌ |
| DELETE | `/api/v1/users/{id}` | ✅ | ✅ | ❌ |

# Sprint Planning — Polaris ERP API

_Última actualización: 2026-02-25 (Sprint 5 en progreso — SystemValue y Product completados)_

---

## Progreso general

| Sprint | Descripción | Avance |
|---|---|---|
| Sprint 1 | Infraestructura base | 100% |
| Sprint 2 | Módulo Users (CRUD) | 78% |
| Sprint 3 | Autenticación JWT | 100% |
| Sprint 4 | Manejo global de errores | 100% |
| Sprint 5 | Módulos de negocio ERP | 40% |
| Sprint 6 | Testing | 0% |
| **Total** | | **70%** |

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
| `UserRepository` con queries para soft delete + `JpaSpecificationExecutor` | ✅ |
| `UserSpecification` para filtrado dinámico | ✅ |
| DTOs con validaciones (`CreateUserRequest`, `UpdateUserRequest`, `UserResponse`, `UserFilter`) | ✅ |
| `UserService` (interfaz + implementación) | ✅ |
| `UserController` con endpoints REST y paginación | ✅ |
| `SecurityConfig` con `PasswordEncoder` (BCrypt) | ✅ |
| `DataInitializer` — seeder de usuario admin por defecto | ✅ |
| Paginación genérica (`PageQuery` + `PageResponse`) en `common/` | ✅ |
| Cambio de contraseña (flujo dedicado con verificación) | ❌ |
| Cambio de email (flujo dedicado con confirmación) | ❌ |

**Avance: 9/11 — 82%**

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
| Manejo de errores de autenticación/autorización | ✅ |

**Avance: 5/5 — 100%**

---

## Sprint 5 — Módulos de negocio ERP
**Estado: 🔄 En progreso**

### SystemValue (Catálogos parametrizables)

| Tarea | Estado |
|---|---|
| Entidad `SystemValue` con soft delete, unique `(catalogType, value)` | ✅ |
| `SystemValueRepository` con `JpaSpecificationExecutor` + queries custom | ✅ |
| `SystemValueSpecification` para filtrado dinámico | ✅ |
| DTOs completos (`CreateSystemValueRequest`, `UpdateSystemValueRequest`, `SystemValueResponse`, `SystemValueFilter`) | ✅ |
| `SystemValueService` (interfaz + implementación) con paginación | ✅ |
| `SystemValueController` con CRUD + endpoint `/catalog/{type}` para dropdowns | ✅ |
| Seed de 20 system values en `DataInitializer` (5 catálogos: PRODUCT_TYPE, PRODUCT_TRACKING, UOM, CURRENCY, DOCUMENT_STATUS) | ✅ |

**Avance: 7/7 — 100%**

### ProductCategory (Árbol jerárquico de categorías)

| Tarea | Estado |
|---|---|
| Entidad `ProductCategory` con relación auto-referencial y cálculo de `level` | ✅ |
| `ProductCategoryRepository` con queries de árbol + `JpaSpecificationExecutor` | ✅ |
| `ProductCategorySpecification` para filtrado dinámico | ✅ |
| DTOs completos (`CreateProductCategoryRequest`, `UpdateProductCategoryRequest`, `ProductCategoryResponse`, `ProductCategoryFilter`) | ✅ |
| `ProductCategoryService` (interfaz + implementación) con árbol (roots/children) | ✅ |
| `ProductCategoryController` con CRUD + `/roots` + `/{id}/children` | ✅ |
| Validación: no eliminar categoría con hijos activos | ✅ |
| Validación: unicidad de nombre dentro del mismo nivel | ✅ |

**Avance: 8/8 — 100%**

### Product (Gestión de productos)

| Tarea | Estado |
|---|---|
| Entidad `Product` con @ManyToOne a `ProductCategory`, constraints y auditoría | ✅ |
| `ProductRepository` con `JpaSpecificationExecutor` + queries de unicidad | ✅ |
| `ProductSpecification` para filtrado dinámico (code, name, productType, categoryId, isActive) | ✅ |
| DTOs completos (`CreateProductRequest`, `UpdateProductRequest`, `ProductResponse`, `ProductFilter`) | ✅ |
| `ProductService` (interfaz + implementación) con paginación y PATCH parcial | ✅ |
| `ProductController` con CRUD paginado y filtros | ✅ |
| Validación: unicidad de `code` y `barcode` | ✅ |
| Validación: categoría debe existir y no estar eliminada | ✅ |
| Módulo Inventory (movimientos de stock) | ❌ |
| Módulo Purchase Orders | ❌ |
| Módulo Sales Orders | ❌ |

**Avance: 8/11 — 73%**

**Avance total Sprint 5: 23/26 — 88%** _(los 3 faltantes son módulos futuros)_

---

## Sprint 6 — Testing
**Estado: ⏳ Pendiente**

| Tarea | Estado |
|---|---|
| Tests unitarios para `UserServiceImpl` | ❌ |
| Tests unitarios para `SystemValueServiceImpl` | ❌ |
| Tests unitarios para `ProductServiceImpl` | ❌ |
| Tests unitarios para `ProductCategoryServiceImpl` | ❌ |
| Tests de integración para `UserController` | ❌ |
| Tests de integración para `SystemValueController` | ❌ |
| Tests de integración para `ProductController` | ❌ |
| Tests unitarios para `JwtService` | ❌ |
| Tests de integración para endpoints de auth | ❌ |
| Configurar base de datos H2 o Testcontainers para tests | ❌ |

**Avance: 0/10 — 0%**

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
| POST | `/api/v1/systemvalues` | ✅ | ✅ | ❌ |
| GET | `/api/v1/systemvalues` | ✅ | ✅ | ❌ |
| GET | `/api/v1/systemvalues/{id}` | ✅ | ✅ | ❌ |
| GET | `/api/v1/systemvalues/catalog/{type}` | ✅ | ✅ | ❌ |
| PATCH | `/api/v1/systemvalues/{id}` | ✅ | ✅ | ❌ |
| DELETE | `/api/v1/systemvalues/{id}` | ✅ | ✅ | ❌ |
| POST | `/api/v1/product-categories` | ✅ | ✅ | ❌ |
| GET | `/api/v1/product-categories` | ✅ | ✅ | ❌ |
| GET | `/api/v1/product-categories/{id}` | ✅ | ✅ | ❌ |
| GET | `/api/v1/product-categories/roots` | ✅ | ✅ | ❌ |
| GET | `/api/v1/product-categories/{id}/children` | ✅ | ✅ | ❌ |
| PATCH | `/api/v1/product-categories/{id}` | ✅ | ✅ | ❌ |
| DELETE | `/api/v1/product-categories/{id}` | ✅ | ✅ | ❌ |
| POST | `/api/v1/products` | ✅ | ✅ | ❌ |
| GET | `/api/v1/products` | ✅ | ✅ | ❌ |
| GET | `/api/v1/products/{id}` | ✅ | ✅ | ❌ |
| PATCH | `/api/v1/products/{id}` | ✅ | ✅ | ❌ |
| DELETE | `/api/v1/products/{id}` | ✅ | ✅ | ❌ |

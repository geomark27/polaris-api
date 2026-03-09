# Technical Reference — Polaris ERP API

_Última actualización: 2026-03-07 (Multi-tenancy implementado — schema por tenant en PostgreSQL)_

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
| API Docs | SpringDoc OpenAPI 3.0 |

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
springdoc-openapi-starter-webmvc-ui 3.0.1
```

---

## Estructura del proyecto

```
src/main/java/com/azenticsys/polaris/
├── PolarisApiApplication.java            # Entry point, @EnableScheduling
├── config/
│   ├── SecurityConfig.java               # FilterChain, CORS, AuthProvider, PasswordEncoder
│   ├── JwtService.java                   # Generación y validación de tokens (incluye tenantSchema claim)
│   ├── JwtAuthenticationFilter.java      # Interceptor: extrae JWT, setea TenantContext
│   ├── DataSourceConfig.java             # Define TenantAwareDataSource como bean @Primary
│   ├── DataInitializer.java              # Seeder: crea tenant "polaris" + admin + system values
│   ├── OpenApiConfig.java                # Swagger / OpenAPI con Bearer JWT
│   └── multitenancy/
│       ├── TenantContext.java            # ThreadLocal: schema name activo en el request
│       ├── TenantAwareDataSource.java    # DataSource wrapper: SET search_path por request
│       └── TenantFilter.java            # Filter @Order(1): resuelve tenant desde X-Tenant-ID header
├── common/
│   ├── exception/
│   │   ├── ApiError.java                 # DTO de respuesta de error estandarizada
│   │   └── GlobalExceptionHandler.java   # @RestControllerAdvice
│   └── pagination/
│       ├── PageQuery.java                # Parámetros de paginación reutilizables
│       └── PageResponse.java             # Envelope genérico de respuesta paginada
├── auth/                                 # Módulo Auth
│   ├── controller/AuthController.java
│   ├── service/AuthService.java + AuthServiceImpl.java
│   ├── entity/RevokedToken.java          # Blacklist de tokens revocados
│   ├── repository/RevokedTokenRepository.java
│   ├── scheduler/TokenCleanupScheduler.java  # Limpieza diaria a medianoche
│   └── dto/LoginRequest, RefreshTokenRequest, AuthResponse
├── user/                                 # Módulo Users
│   ├── controller/UserController.java
│   ├── service/UserService.java + UserServiceImpl.java + UserDetailsServiceImpl.java
│   ├── repository/UserRepository.java + UserSpecification.java
│   ├── entity/User.java
│   └── dto/CreateUserRequest, UpdateUserRequest, UserResponse, UserFilter
├── systemvalue/                          # Módulo SystemValue (catálogos parametrizables)
│   ├── controller/SystemValueController.java
│   ├── service/SystemValueService.java + SystemValueServiceImpl.java
│   ├── repository/SystemValueRepository.java + SystemValueSpecification.java
│   ├── entity/SystemValue.java
│   └── dto/CreateSystemValueRequest, UpdateSystemValueRequest, SystemValueResponse, SystemValueFilter
├── tenant/                               # Módulo Tenant (landlord — gestión de organizaciones)
│   ├── controller/TenantController.java
│   ├── service/TenantService.java + TenantServiceImpl.java
│   ├── repository/TenantRepository.java
│   ├── entity/Tenant.java                # @Table(schema="landlord") — identidad de cada org
│   └── dto/CreateTenantRequest, TenantResponse
└── product/                              # Módulo Product
    ├── controller/ProductController.java + ProductCategoryController.java
    ├── service/ProductService.java + ProductServiceImpl.java
    │           ProductCategoryService.java + ProductCategoryServiceImpl.java
    ├── repository/ProductRepository.java + ProductSpecification.java
    │             ProductCategoryRepository.java + ProductCategorySpecification.java
    ├── entity/Product.java + ProductCategory.java
    └── dto/CreateProductRequest, UpdateProductRequest, ProductResponse, ProductFilter
            CreateProductCategoryRequest, UpdateProductCategoryRequest,
            ProductCategoryResponse, ProductCategoryFilter
```

### Patrón de arquitectura

Arquitectura en capas por módulo de dominio:

```
Controller  →  Service (interfaz)  →  ServiceImpl  →  Repository  →  Entity (BD)
    ↑                                     ↑                ↑
  DTOs                            @Transactional    JpaSpecificationExecutor
                                                    + Specification (filtros)
```

- Cada módulo es autocontenido: controller, service, repo, entity y DTOs viven juntos.
- Las interfaces de servicio desacoplan el controller de la implementación.
- Los DTOs son Java Records (inmutables, sin boilerplate).
- La inyección de dependencias es siempre por constructor (`@RequiredArgsConstructor` de Lombok).
- El filtrado dinámico usa JPA Criteria API mediante clases `XxxSpecification`.
- La paginación es genérica y reutilizable (`PageQuery` / `PageResponse`).

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
| `createdAt` | `LocalDateTime` | `created_at` | NOT NULL, no actualizable |
| `updatedAt` | `LocalDateTime` | `updated_at` | NOT NULL, auto-gestionado |
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
    UUID id, String username, String email, String phoneNumber,
    boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt
)
```

**`UserFilter`**
```java
record UserFilter(String username, String email, String phoneNumber)
```

---

## Módulo: Auth

### DTOs

**`LoginRequest`**
```java
record LoginRequest(String username, String password)   // ambos @NotBlank
```

**`RefreshTokenRequest`**
```java
record RefreshTokenRequest(String refreshToken)   // @NotBlank
```

**`AuthResponse`**
```java
record AuthResponse(UUID userId, String username, String accessToken, String refreshToken)
```

---

## Módulo: SystemValue

### Entidad `SystemValue`

| Campo | Tipo Java | Columna BD | Restricciones |
|---|---|---|---|
| `id` | `UUID` | `id` | PK, UUIDv4 auto |
| `catalogType` | `String` | `catalog_type` | NOT NULL, max 50, UPPERCASE |
| `value` | `String` | `value` | NOT NULL, max 100, UPPERCASE |
| `label` | `String` | `label` | NOT NULL, max 200 |
| `description` | `String` | `description` | nullable, TEXT |
| `displayOrder` | `int` | `display_order` | NOT NULL, default 0 |
| `isActive` | `boolean` | `is_active` | NOT NULL, default true |
| `createdAt` | `LocalDateTime` | `created_at` | NOT NULL, no actualizable |
| `updatedAt` | `LocalDateTime` | `updated_at` | NOT NULL, auto-gestionado |
| `deletedAt` | `LocalDateTime` | `deleted_at` | nullable, soft delete |

**Unique constraint:** `(catalog_type, value)`
**Index:** `catalog_type`

### DTOs

**`CreateSystemValueRequest`**
```java
record CreateSystemValueRequest(
    String catalogType,   // @NotBlank, @Size(max=50)
    String value,         // @NotBlank, @Size(max=100)
    String label,         // @NotBlank, @Size(max=200)
    String description,   // nullable
    int displayOrder
)
```

**`UpdateSystemValueRequest`**
```java
record UpdateSystemValueRequest(
    String label,         // @Size(max=200), nullable
    String description,   // nullable
    Integer displayOrder  // nullable
)
```

**`SystemValueResponse`**
```java
record SystemValueResponse(
    UUID id, String catalogType, String value, String label,
    String description, int displayOrder, boolean isActive,
    LocalDateTime createdAt, LocalDateTime updatedAt
)
```

**`SystemValueFilter`**
```java
record SystemValueFilter(String catalogType, String value, String label, Boolean isActive)
```

---

## Módulo: Product

### Entidad `ProductCategory`

| Campo | Tipo Java | Columna BD | Restricciones |
|---|---|---|---|
| `id` | `UUID` | `id` | PK, UUIDv4 auto |
| `name` | `String` | `name` | NOT NULL, max 200 |
| `description` | `String` | `description` | nullable, TEXT |
| `parent` | `ProductCategory` | `parent_id` | nullable, FK auto-referencial |
| `children` | `List<ProductCategory>` | — | OneToMany, cascade |
| `level` | `int` | `level` | NOT NULL, calculado en @PrePersist |
| `displayOrder` | `int` | `display_order` | NOT NULL, default 0 |
| `isActive` | `boolean` | `is_active` | NOT NULL, default true |
| `createdAt` / `updatedAt` / `deletedAt` | `LocalDateTime` | — | auditoría estándar |

**Indexes:** `parent_id`, `is_active`

### Entidad `Product`

| Campo | Tipo Java | Columna BD | Restricciones |
|---|---|---|---|
| `id` | `UUID` | `id` | PK, UUIDv4 auto |
| `code` | `String` | `code` | NOT NULL, UNIQUE, max 50, UPPERCASE |
| `name` | `String` | `name` | NOT NULL, max 200 |
| `description` | `String` | `description` | nullable, TEXT |
| `barcode` | `String` | `barcode` | nullable, UNIQUE, max 100 |
| `productType` | `String` | `product_type` | NOT NULL, max 100 (ref PRODUCT_TYPE) |
| `category` | `ProductCategory` | `category_id` | nullable, FK |
| `canBeSold` | `boolean` | `can_be_sold` | default true |
| `canBePurchased` | `boolean` | `can_be_purchased` | default true |
| `canBeManufactured` | `boolean` | `can_be_manufactured` | default false |
| `tracking` | `String` | `tracking` | NOT NULL, default `NONE` (ref PRODUCT_TRACKING) |
| `unitOfMeasure` | `String` | `unit_of_measure` | NOT NULL, default `PCS` (ref UOM) |
| `purchaseUom` | `String` | `purchase_uom` | nullable (ref UOM) |
| `salePrice` | `BigDecimal` | `sale_price` | precision 18,4, default 0 |
| `costPrice` | `BigDecimal` | `cost_price` | precision 18,4, default 0 |
| `currency` | `String` | `currency` | NOT NULL, max 10, default `USD` (ref CURRENCY) |
| `weight` | `BigDecimal` | `weight` | nullable, precision 10,4, kg |
| `volume` | `BigDecimal` | `volume` | nullable, precision 10,4, m³ |
| `minStock` | `BigDecimal` | `min_stock` | precision 18,4, default 0 |
| `maxStock` | `BigDecimal` | `max_stock` | precision 18,4, default 0 |
| `isActive` / `createdAt` / `updatedAt` / `deletedAt` | — | — | auditoría estándar |

**Unique constraints:** `code`, `barcode`
**Indexes:** `category_id`, `product_type`, `is_active`

### DTOs

**`CreateProductRequest`**
```java
record CreateProductRequest(
    String code,              // @NotBlank, @Size(max=50)
    String name,              // @NotBlank, @Size(max=200)
    String description,       // nullable
    String barcode,           // @Size(max=100), nullable
    String productType,       // @NotBlank
    UUID categoryId,          // nullable
    boolean canBeSold,
    boolean canBePurchased,
    boolean canBeManufactured,
    String tracking,          // nullable, default NONE
    String unitOfMeasure,     // nullable, default PCS
    String purchaseUom,       // nullable
    BigDecimal salePrice,     // @DecimalMin(0.0)
    BigDecimal costPrice,     // @DecimalMin(0.0)
    String currency,          // nullable, default USD
    BigDecimal weight,        // @DecimalMin(0.0), nullable
    BigDecimal volume,        // @DecimalMin(0.0), nullable
    BigDecimal minStock,      // @DecimalMin(0.0)
    BigDecimal maxStock       // @DecimalMin(0.0)
)
```

**`UpdateProductRequest`** — mismos campos que Create pero todos opcionales (PATCH parcial).

**`ProductResponse`**
```java
record ProductResponse(
    UUID id, String code, String name, String description, String barcode,
    String productType, UUID categoryId, String categoryName,
    boolean canBeSold, boolean canBePurchased, boolean canBeManufactured,
    String tracking, String unitOfMeasure, String purchaseUom,
    BigDecimal salePrice, BigDecimal costPrice, String currency,
    BigDecimal weight, BigDecimal volume, BigDecimal minStock, BigDecimal maxStock,
    boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt
)
```

**`CreateProductCategoryRequest`**
```java
record CreateProductCategoryRequest(
    String name,         // @NotBlank, @Size(max=200)
    String description,  // nullable
    UUID parentId,       // nullable
    int displayOrder
)
```

**`UpdateProductCategoryRequest`** — name, description, displayOrder opcionales.

**`ProductCategoryResponse`**
```java
record ProductCategoryResponse(
    UUID id, String name, String description,
    UUID parentId, String parentName, int level, int displayOrder,
    boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt
)
```

---

## Endpoints

Base URL: `http://localhost:8080`

Todos los endpoints excepto `/api/v1/auth/**` requieren `Authorization: Bearer <accessToken>`.

---

### Auth — `POST /api/v1/auth/login`

**Request body**
```json
{ "username": "admin", "password": "Admin1234!" }
```
**Response `200`**
```json
{
  "userId": "uuid",
  "username": "admin",
  "accessToken": "eyJ...",
  "refreshToken": "eyJ..."
}
```
**Errores:** `400` campos vacíos · `401` credenciales inválidas o cuenta desactivada

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin1234!"}'
```

---

### Auth — `POST /api/v1/auth/refresh`

**Request body**
```json
{ "refreshToken": "eyJ..." }
```
**Response `200`** — mismo schema que login

**Errores:** `401` token inválido, expirado, o se recibió un accessToken

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJ..."}'
```

---

### Auth — `POST /api/v1/auth/logout`

**Headers:** `Authorization: Bearer <accessToken>`

**Response `204 No Content`**

**Errores:** `401` token inválido o expirado

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJ..."
```

---

### Users — `POST /api/v1/users`

**Request body**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "secret123",
  "phoneNumber": "+57 300 123 4567"
}
```
**Response `201`**
```json
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "phoneNumber": "+57 300 123 4567",
  "isActive": true,
  "createdAt": "2026-02-25T10:00:00",
  "updatedAt": "2026-02-25T10:00:00"
}
```
**Errores:** `400` validación fallida · `400` username o email ya en uso

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"username":"john_doe","email":"john@example.com","password":"secret123"}'
```

---

### Users — `GET /api/v1/users`

**Query params:** `page` (def: 0) · `size` (def: 10) · `sortBy` (def: createdAt) · `sortDir` (def: desc) · `username` · `email` · `phoneNumber`

**Response `200`** — `PageResponse<UserResponse>`

```bash
curl "http://localhost:8080/api/v1/users?page=0&size=10&username=john" \
  -H "Authorization: Bearer eyJ..."
```

---

### Users — `GET /api/v1/users/{id}`

**Response `200`** — `UserResponse`

**Errores:** `400` no encontrado o eliminado

---

### Users — `PATCH /api/v1/users/{id}`

**Request body** (todos opcionales)
```json
{ "username": "new_name", "phoneNumber": "+57 311 999 8888" }
```
**Response `200`** — `UserResponse`

**Errores:** `400` nuevo username ya tomado · `400` usuario no encontrado

---

### Users — `DELETE /api/v1/users/{id}`

**Response `204`** (soft delete)

**Errores:** `400` usuario no encontrado o ya eliminado

---

### SystemValues — `POST /api/v1/systemvalues`

**Request body**
```json
{
  "catalogType": "PRODUCT_TYPE",
  "value": "BUNDLE",
  "label": "Paquete",
  "description": "Conjunto de productos agrupados",
  "displayOrder": 4
}
```
**Response `201`** — `SystemValueResponse`

**Errores:** `400` validación · `400` combinación catalogType+value ya existe

```bash
curl -X POST http://localhost:8080/api/v1/systemvalues \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"catalogType":"PRODUCT_TYPE","value":"BUNDLE","label":"Paquete","displayOrder":4}'
```

---

### SystemValues — `GET /api/v1/systemvalues`

**Query params:** `page` · `size` · `sortBy` (def: displayOrder) · `sortDir` (def: asc) · `catalogType` · `value` · `label` · `isActive`

**Response `200`** — `PageResponse<SystemValueResponse>`

```bash
curl "http://localhost:8080/api/v1/systemvalues?catalogType=UOM" \
  -H "Authorization: Bearer eyJ..."
```

---

### SystemValues — `GET /api/v1/systemvalues/catalog/{catalogType}`

Lista completa sin paginar de un catálogo, ordenada por `displayOrder`. Ideal para poblar dropdowns en el frontend.

**Response `200`** — `List<SystemValueResponse>`

```bash
curl http://localhost:8080/api/v1/systemvalues/catalog/UOM \
  -H "Authorization: Bearer eyJ..."
```

---

### SystemValues — `GET /api/v1/systemvalues/{id}`

**Response `200`** — `SystemValueResponse`

**Errores:** `400` no encontrado o eliminado

---

### SystemValues — `PATCH /api/v1/systemvalues/{id}`

**Request body** (todos opcionales)
```json
{ "label": "Nuevo label", "description": "Descripción actualizada", "displayOrder": 2 }
```
**Response `200`** — `SystemValueResponse`

---

### SystemValues — `DELETE /api/v1/systemvalues/{id}`

**Response `204`** (soft delete)

---

### ProductCategories — `POST /api/v1/product-categories`

**Request body**
```json
{
  "name": "Electrónica",
  "description": "Dispositivos electrónicos",
  "parentId": null,
  "displayOrder": 0
}
```
**Response `201`** — `ProductCategoryResponse`

**Errores:** `400` nombre ya existe en el mismo nivel · `400` parent no encontrado

```bash
curl -X POST http://localhost:8080/api/v1/product-categories \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"name":"Electrónica","displayOrder":0}'
```

---

### ProductCategories — `GET /api/v1/product-categories`

**Query params:** `page` · `size` · `sortBy` (def: displayOrder) · `sortDir` (def: asc) · `name` · `parentId` · `level` · `isActive`

**Response `200`** — `PageResponse<ProductCategoryResponse>`

---

### ProductCategories — `GET /api/v1/product-categories/roots`

Lista todas las categorías raíz (sin padre), ordenadas por `displayOrder`.

**Response `200`** — `List<ProductCategoryResponse>`

```bash
curl http://localhost:8080/api/v1/product-categories/roots \
  -H "Authorization: Bearer eyJ..."
```

---

### ProductCategories — `GET /api/v1/product-categories/{id}/children`

Lista los hijos directos de una categoría.

**Response `200`** — `List<ProductCategoryResponse>`

```bash
curl http://localhost:8080/api/v1/product-categories/uuid/children \
  -H "Authorization: Bearer eyJ..."
```

---

### ProductCategories — `GET /api/v1/product-categories/{id}`

**Response `200`** — `ProductCategoryResponse`

---

### ProductCategories — `PATCH /api/v1/product-categories/{id}`

**Request body** (todos opcionales)
```json
{ "name": "Nuevo nombre", "description": "...", "displayOrder": 1 }
```
**Response `200`** — `ProductCategoryResponse`

---

### ProductCategories — `DELETE /api/v1/product-categories/{id}`

**Response `204`** (soft delete)

**Errores:** `400` categoría tiene hijos activos

---

### Products — `POST /api/v1/products`

**Request body**
```json
{
  "code": "LAPTOP-001",
  "name": "Laptop Gaming",
  "productType": "STANDARD",
  "categoryId": "uuid-categoria",
  "canBeSold": true,
  "canBePurchased": true,
  "tracking": "SERIAL",
  "unitOfMeasure": "PCS",
  "salePrice": 1299.99,
  "costPrice": 850.00,
  "currency": "USD",
  "minStock": 5,
  "maxStock": 50
}
```
**Response `201`** — `ProductResponse`

**Errores:** `400` validación · `400` code o barcode ya existen · `400` categoría no encontrada

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{"code":"LAPTOP-001","name":"Laptop Gaming","productType":"STANDARD","salePrice":1299.99,"currency":"USD"}'
```

---

### Products — `GET /api/v1/products`

**Query params:** `page` · `size` · `sortBy` (def: createdAt) · `sortDir` (def: desc) · `code` · `name` · `productType` · `categoryId` · `isActive`

**Response `200`** — `PageResponse<ProductResponse>`

```bash
curl "http://localhost:8080/api/v1/products?productType=STANDARD&isActive=true" \
  -H "Authorization: Bearer eyJ..."
```

---

### Products — `GET /api/v1/products/{id}`

**Response `200`** — `ProductResponse`

**Errores:** `400` no encontrado o eliminado

---

### Products — `PATCH /api/v1/products/{id}`

**Request body** — todos los campos opcionales (PATCH parcial)

**Response `200`** — `ProductResponse`

**Errores:** `400` code o barcode ya en uso por otro producto

---

### Products — `DELETE /api/v1/products/{id}`

**Response `204`** (soft delete)

---

## Seguridad

**Estrategia:** Stateless. No hay sesión en el servidor. Cada request debe incluir un `accessToken` válido en el header `Authorization`.

**Rutas públicas:** solo `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`. El resto requiere JWT.

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
  "timestamp": "2026-02-25T10:00:00"
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

`DataInitializer` implementa `CommandLineRunner` y se ejecuta al arrancar. Es idempotente: no re-siembra si ya existen datos.

**Lógica al arrancar:**
1. Si `users` está vacía → crea el usuario admin con BCrypt.
2. Si `system_values` está vacía → siembra 20 valores en 5 catálogos.

**Credenciales del admin por defecto** (configurables vía `.env`):

| Variable | Valor por defecto |
|---|---|
| `ADMIN_USERNAME` | `admin` |
| `ADMIN_EMAIL` | `admin@polaris.local` |
| `ADMIN_PASSWORD` | `Admin1234!` |

> **Importante:** cambiar `ADMIN_PASSWORD` antes de desplegar en cualquier entorno que no sea local.

---

## Multi-tenancy — Arquitectura

### Estrategia: Schema por tenant en PostgreSQL

Cada organización (tenant) obtiene su propio schema PostgreSQL. Un solo cluster PostgreSQL, una sola base de datos, schemas separados.

```
polaris_db
├── schema: landlord          ← datos del sistema SaaS
│   ├── tenants               ← registro de organizaciones (slug, schemaName, email)
│   └── revoked_tokens        ← blacklist de JWTs revocados (global)
│
├── schema: public            ← tablas template (creadas por Hibernate DDL al arrancar)
│   ├── users                 ← template (no contiene datos reales)
│   ├── products              ← template
│   └── ...
│
├── schema: t_polaris         ← tenant "polaris" (desarrollo por defecto)
│   ├── users
│   ├── products
│   └── ...
│
└── schema: t_torresytorres   ← tenant "torres-y-torres" (ejemplo real)
    ├── users
    ├── products
    └── ...
```

### Resolución del tenant por request

```
1. TenantFilter (@Order 1):
   Lee X-Tenant-ID header (slug, ej: "torresytorres")
   → Busca en landlord.tenants → obtiene schemaName
   → TenantContext.set("t_torresytorres")

2. JwtAuthenticationFilter:
   Extrae tenantSchema del claim JWT (sobreescribe TenantContext — más seguro)
   → TenantContext.set("t_torresytorres")

3. TenantAwareDataSource.getConnection():
   Lee TenantContext.get() → "t_torresytorres"
   → SET search_path TO t_torresytorres, landlord, public

4. Todas las queries del request resuelven contra t_torresytorres.*
   Entidades landlord (RevokedToken, Tenant) usan schema explícito en @Table
   → Siempre acceden landlord.* independientemente del search_path
```

### Tenant slug vs schemaName

| Concepto | Ejemplo | Uso |
|---|---|---|
| `slug` | `torres-y-torres` | URL-friendly, visible para el usuario |
| `schemaName` | `t_torres_y_torres` | Schema PostgreSQL interno |
| Derivación | `"t_" + slug.replace("-", "_")` | Automático al crear |

### Creación de tenant (nueva organización)

```
POST /api/v1/tenants/register
{ "name": "Torres y Torres S.A.", "slug": "torres-y-torres", "email": "admin@torresytorres.com" }

1. Valida slug único + email único
2. Inserta en landlord.tenants
3. CREATE SCHEMA IF NOT EXISTS t_torres_y_torres
4. Para cada tabla tenant: CREATE TABLE t_torres_y_torres.{tabla} (LIKE public.{tabla} INCLUDING ALL)
5. Agrega FK products → product_categories en el nuevo schema
```

### Flujo de login (tenant)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "X-Tenant-ID: torres-y-torres" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin1234!"}'

# Response incluye tenantSlug en el body y tenantSchema embebido en el JWT
# Requests autenticados posteriores: el JWT lleva el tenantSchema, no es necesario el header
```

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

Al arrancar, Spring ejecuta `schema.sql` (crea el schema `landlord`) y luego Hibernate DDL crea las tablas. El schema `public` actúa como template para clonar al crear nuevos tenants.

En producción cambiar `ddl-auto` a `validate` y gestionar migraciones con Flyway.

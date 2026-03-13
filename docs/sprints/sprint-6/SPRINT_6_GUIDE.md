# Sprint 6 - Guia de Implementacion: Configuracion de Empresa

Objetivo: implementar los datos maestros de configuracion de la empresa emisora, sus establecimientos fisicos, puntos de emision de documentos y bodegas de inventario, ademas de la configuracion general del tenant.

---

## 1. Contexto de negocio

### Por que Company no es lo mismo que Tenant

El `Tenant` es una entidad de infraestructura SaaS (schema, slug, estado de suscripcion). La `Company` son los datos fiscales y legales de la empresa que opera dentro de ese tenant: RUC, razon social, logo, etc.

Separar ambos permite:
- Futura implementacion multiempresa (un tenant = N empresas)
- No mezclar logica de SaaS con logica de negocio
- Que el frontend muestre datos fiscales sin tocar la entidad Tenant

### Jerarquia de ubicaciones

```
Company (empresa emisora)
 └─ Establishment (matriz y sucursales — codigo SRI 3 digitos)
     ├─ EmissionPoint (puntos de emision — codigo SRI 3 digitos)
     │   └─ Secuenciales por tipo de documento (factura, NC, ND, retencion, guia)
     └─ Warehouse (bodega — concepto logistico, no fiscal)
```

Un numero de documento en Ecuador tiene la forma:
```
001 - 001 - 000000001
 ↑     ↑        ↑
Estab. Pto.Em.  Secuencial
```

### Diferencias clave

| Concepto | Proposito | Codigo SRI | Gestiona stock |
|---|---|---|---|
| Establishment | Ubicacion fiscal registrada en SRI | Si (001, 002...) | No |
| EmissionPoint | Caja/terminal que emite documentos | Si (001, 002...) | No |
| Warehouse | Almacen fisico de mercaderia | No | Si |

Los tres pueden coexistir en la misma ubicacion fisica pero son entidades independientes. Una bodega central puede abastecer a varios establecimientos. Un establecimiento puede tener varios puntos de emision (cajeros).

---

## 2. Modulos a implementar

### 2.1 Company (Datos de la empresa emisora)

**Tabla:** `companies`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK auto |
| `trade_name` | VARCHAR(200) | Nombre comercial |
| `business_name` | VARCHAR(300) | Razon social (para documentos) |
| `ruc` | VARCHAR(13) | RUC Ecuador — unique |
| `address` | VARCHAR(500) | Direccion principal |
| `phone` | VARCHAR(20) | Telefono |
| `email` | VARCHAR(150) | Email de contacto |
| `website` | VARCHAR(255) | Sitio web (opcional) |
| `logo_url` | VARCHAR(500) | URL del logo (opcional) |
| `is_active` | BOOLEAN | Default true |
| `created_at` / `updated_at` | TIMESTAMP | Auditoria |

**Reglas de negocio:**
- Solo puede existir 1 company activa por tenant (MVP). Preparar para N en el futuro.
- `ruc` debe ser unico dentro del tenant.
- Validar formato RUC: 13 digitos, termina en 001.

**Endpoints:**
```
GET    /api/v1/companies           → listar (para futura multiempresa)
POST   /api/v1/companies           → crear empresa → 201
GET    /api/v1/companies/{id}      → obtener por id
PUT    /api/v1/companies/{id}      → actualizar completo
DELETE /api/v1/companies/{id}      → soft delete
```

**Seed:** No crear por defecto. El admin la configura tras registrar el tenant.

---

### 2.2 Establishment (Matriz y Sucursales)

**Tabla:** `establishments`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK auto |
| `company_id` | UUID | FK → companies |
| `code` | VARCHAR(3) | Codigo SRI (001=matriz, 002...) — unique por company |
| `name` | VARCHAR(200) | Nombre descriptivo (ej: "Matriz Quito") |
| `address` | VARCHAR(500) | Direccion fisica |
| `city` | VARCHAR(100) | Ciudad |
| `phone` | VARCHAR(20) | Telefono (opcional) |
| `is_main` | BOOLEAN | True si es la matriz (solo una por company) |
| `is_active` | BOOLEAN | Default true |
| `created_at` / `updated_at` | TIMESTAMP | Auditoria |
| `deleted_at` | TIMESTAMP | Soft delete |

**Reglas de negocio:**
- `code` debe ser unico dentro de la misma `company_id`.
- Solo puede haber un `is_main = true` por company.
- No eliminar si tiene EmissionPoints o Warehouses activos.
- Al crear la primera Establishment de una Company, marcarla como `is_main = true` automaticamente.

**Endpoints:**
```
GET    /api/v1/companies/{companyId}/establishments        → listar
POST   /api/v1/companies/{companyId}/establishments        → crear → 201
GET    /api/v1/companies/{companyId}/establishments/{id}   → obtener
PATCH  /api/v1/companies/{companyId}/establishments/{id}   → actualizar
DELETE /api/v1/companies/{companyId}/establishments/{id}   → soft delete
```

---

### 2.3 EmissionPoint (Puntos de Emision)

**Tabla:** `emission_points`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK auto |
| `establishment_id` | UUID | FK → establishments |
| `code` | VARCHAR(3) | Codigo punto de emision (001, 002...) — unique por establishment |
| `name` | VARCHAR(200) | Nombre descriptivo (ej: "Caja 1") |
| `is_active` | BOOLEAN | Default true |
| `created_at` / `updated_at` | TIMESTAMP | Auditoria |
| `deleted_at` | TIMESTAMP | Soft delete |

**Secuenciales por tipo de documento:**

**Tabla:** `emission_point_sequences`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK auto |
| `emission_point_id` | UUID | FK → emission_points |
| `document_type` | VARCHAR(50) | Enum: INVOICE, CREDIT_NOTE, DEBIT_NOTE, RETENTION, DELIVERY_NOTE |
| `last_sequence` | BIGINT | Ultimo numero usado. Default 0 |

Combinacion `(emission_point_id, document_type)` debe ser unica.

El numero de documento se construye: `{estab.code}-{ep.code}-{sequence 9 digitos cero-padded}`

Ejemplo: `001-001-000000042`

**Reglas de negocio:**
- `code` unico dentro del mismo `establishment_id`.
- Al crear un EmissionPoint, crear automaticamente los 5 registros de secuencia (uno por tipo de documento) con `last_sequence = 0`.
- No eliminar si tiene documentos emitidos.

**Endpoints:**
```
GET    /api/v1/establishments/{establishmentId}/emission-points        → listar
POST   /api/v1/establishments/{establishmentId}/emission-points        → crear → 201
GET    /api/v1/establishments/{establishmentId}/emission-points/{id}   → obtener
PATCH  /api/v1/establishments/{establishmentId}/emission-points/{id}   → actualizar
DELETE /api/v1/establishments/{establishmentId}/emission-points/{id}   → soft delete
```

---

### 2.4 Warehouse (Bodegas)

**Tabla:** `warehouses`

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK auto |
| `establishment_id` | UUID | FK → establishments (nullable — bodega puede ser independiente) |
| `code` | VARCHAR(20) | Codigo interno (ej: "BOD-01") — unique en tenant |
| `name` | VARCHAR(200) | Nombre descriptivo |
| `address` | VARCHAR(500) | Direccion (opcional) |
| `is_main` | BOOLEAN | Bodega principal del tenant. Default false |
| `is_active` | BOOLEAN | Default true |
| `created_at` / `updated_at` | TIMESTAMP | Auditoria |
| `deleted_at` | TIMESTAMP | Soft delete |

**Reglas de negocio:**
- `code` unico dentro del tenant.
- Solo puede haber un `is_main = true` por tenant.
- No eliminar si tiene stock activo (verificar en Sprint 8).
- Seed: crear bodega principal ("Bodega Principal", `is_main = true`) al registrar el tenant.

**Endpoints:**
```
GET    /api/v1/warehouses           → listar (paginado, filtros: isActive, isMain)
POST   /api/v1/warehouses           → crear → 201
GET    /api/v1/warehouses/{id}      → obtener
PATCH  /api/v1/warehouses/{id}      → actualizar
DELETE /api/v1/warehouses/{id}      → soft delete
```

---

### 2.5 GeneralSettings (Configuracion general del tenant)

**Tabla:** `general_settings`

Un solo registro por tenant. No tiene soft delete — siempre existe.

| Campo | Tipo | Notas |
|---|---|---|
| `id` | UUID | PK auto |
| `currency` | VARCHAR(3) | Codigo ISO 4217 (ej: "USD") |
| `timezone` | VARCHAR(50) | Zona horaria IANA (ej: "America/Guayaquil") |
| `language` | VARCHAR(5) | Codigo ISO (ej: "es-EC") |
| `default_tax_rate` | DECIMAL(5,2) | IVA por defecto (ej: 15.00) |
| `decimal_places` | INTEGER | Decimales en precios. Default 2 |
| `date_format` | VARCHAR(20) | Formato fecha UI (ej: "dd/MM/yyyy") |
| `updated_at` | TIMESTAMP | Auditoria |

**Reglas de negocio:**
- Solo existe 1 registro por tenant. No hay POST ni DELETE.
- Si no existe al arrancar → el seed lo crea con valores por defecto.
- El frontend la usa para formatear montos, fechas y calcular impuestos.

**Endpoints:**
```
GET  /api/v1/settings    → obtener configuracion del tenant
PUT  /api/v1/settings    → actualizar configuracion completa
```

**Seed por defecto al crear tenant:**
```
currency:         USD
timezone:         America/Guayaquil
language:         es-EC
default_tax_rate: 15.00
decimal_places:   2
date_format:      dd/MM/yyyy
```

---

## 3. Relaciones y FK entre modulos

```
companies
 └─ establishments (company_id → companies.id)
     ├─ emission_points (establishment_id → establishments.id)
     │   └─ emission_point_sequences (emission_point_id → emission_points.id)
     └─ warehouses (establishment_id → establishments.id, nullable)
```

`general_settings` es independiente (no tiene FK a company — es configuracion del tenant).

---

## 4. Orden de implementacion recomendado

1. `GeneralSettings` — mas simple, sin dependencias
2. `Company` — prerequisito para Establishment
3. `Establishment` — prerequisito para EmissionPoint y Warehouse
4. `Warehouse` — necesario para Sprint 8 (inventario)
5. `EmissionPoint` + `EmissionPointSequence` — necesario para Sprint 9 (ventas)

---

## 5. Tablas a agregar en TENANT_TABLES (TenantServiceImpl)

Al registrar un nuevo tenant, se deben crear estas tablas en su schema.
Agregar en `TenantServiceImpl.TENANT_TABLES`:

```java
"companies",
"establishments",
"emission_points",
"emission_point_sequences",
"warehouses",
"general_settings"
```

Y agregar la FK entre `emission_points` y `establishments` similar al patron existente de productos.

---

## 6. Seeds al crear tenant (DataInitializer o TenantServiceImpl)

Al registrar un tenant nuevo, ejecutar automaticamente:

1. Crear `GeneralSettings` con valores por defecto (USD, America/Guayaquil, 15% IVA)
2. Crear `Warehouse` principal (`code="BOD-001"`, `is_main=true`, `name="Bodega Principal"`)

No crear Company, Establishment ni EmissionPoint automaticamente — el admin los configura.

---

## 7. Checklist de implementacion

### GeneralSettings
- [ ] Entidad `GeneralSettings`
- [ ] `GeneralSettingsRepository`
- [ ] `GeneralSettingsService` (interfaz + impl) — solo `find` y `update`
- [ ] `GeneralSettingsController` — `GET /api/v1/settings` y `PUT /api/v1/settings`
- [ ] DTOs: `UpdateGeneralSettingsRequest`, `GeneralSettingsResponse`
- [ ] Seed en `DataInitializer`

### Company
- [ ] Entidad `Company`
- [ ] `CompanyRepository` con `findByRucAndDeletedAtIsNull`
- [ ] `CompanyService` (interfaz + impl) con CRUD
- [ ] `CompanyController` con endpoints REST
- [ ] DTOs: `CreateCompanyRequest`, `UpdateCompanyRequest`, `CompanyResponse`
- [ ] Validacion formato RUC

### Establishment
- [ ] Entidad `Establishment` con FK a `Company`
- [ ] `EstablishmentRepository`
- [ ] `EstablishmentService` (interfaz + impl) con logica de `is_main`
- [ ] `EstablishmentController` anidado bajo `/companies/{companyId}/`
- [ ] DTOs correspondientes

### Warehouse
- [ ] Entidad `Warehouse` con FK nullable a `Establishment`
- [ ] `WarehouseRepository`
- [ ] `WarehouseService` (interfaz + impl) con logica de `is_main`
- [ ] `WarehouseController` con endpoints REST
- [ ] DTOs correspondientes
- [ ] Seed de bodega principal en `DataInitializer`

### EmissionPoint
- [ ] Entidad `EmissionPoint` con FK a `Establishment`
- [ ] Entidad `EmissionPointSequence` con FK a `EmissionPoint`
- [ ] `EmissionPointRepository` y `EmissionPointSequenceRepository`
- [ ] `EmissionPointService` — al crear EP, genera automaticamente los 5 registros de secuencia
- [ ] `EmissionPointController` anidado bajo `/establishments/{establishmentId}/`
- [ ] DTOs correspondientes
- [ ] Metodo `getNextSequence(emissionPointId, documentType)` con `@Transactional` y bloqueo pesimista (evitar duplicados)

### Infraestructura
- [ ] Agregar las 6 tablas a `TenantServiceImpl.TENANT_TABLES`
- [ ] Agregar FK de `emission_points → establishments` en el bloque DO $$ del schema creation
- [ ] Agregar FK de `warehouses → establishments` (nullable) si se decide incluir

---

## 8. Nota sobre bloqueo pesimista en secuenciales

El metodo `getNextSequence` debe usar `SELECT FOR UPDATE` para evitar dos documentos con el mismo numero en concurrencia:

```java
@Transactional
public String getNextSequence(UUID emissionPointId, DocumentType type) {
    EmissionPointSequence seq = repository
        .findByEmissionPointIdAndDocumentTypeForUpdate(emissionPointId, type)
        .orElseThrow(...);
    seq.setLastSequence(seq.getLastSequence() + 1);
    repository.save(seq);
    // formatear: 001-001-000000042
    return format(seq);
}
```

En Spring Data JPA usar `@Lock(LockModeType.PESSIMISTIC_WRITE)` en el repository.

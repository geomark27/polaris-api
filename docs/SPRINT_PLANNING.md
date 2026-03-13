# Sprint Planning — Polaris ERP API

_Última actualización: 2026-03-12_

---

## Progreso general

| Sprint | Descripción | Avance |
|---|---|---|
| Sprint 0 | Multi-tenancy (core) | 100% ✅ |
| Sprint 1 | Infraestructura base | 100% ✅ |
| Sprint 2 | Módulo Users (CRUD) | 82% 🔄 |
| Sprint 3 | Autenticación JWT | 100% ✅ |
| Sprint 4 | Manejo global de errores | 100% ✅ |
| Sprint 5 | Catálogos y Productos | 100% ✅ |
| Sprint 6 | Configuración de empresa | 0% ⏳ |
| Sprint 7 | Clientes y Proveedores | 0% ⏳ |
| Sprint 8 | Inventario | 0% ⏳ |
| Sprint 9 | Cotizaciones y Ventas | 0% ⏳ |
| Sprint 10 | Compras | 0% ⏳ |
| Sprint 11 | Devoluciones y Documentos electrónicos | 0% ⏳ |
| Sprint 12 | Caja, Gastos y Contabilidad | 0% ⏳ |
| Sprint 13 | Roles y Permisos | 0% ⏳ |
| Sprint 14 | Promociones (comisiones, descuentos, gift cards) | 0% ⏳ |
| Sprint 15 | Logística (delivery, guías de remisión) | 0% ⏳ |
| Sprint 16 | RRHH y Nómina | 0% ⏳ |
| Sprint 17 | Reportes | 0% ⏳ |
| Sprint 18 | Integraciones externas | 0% ⏳ |
| Sprint 19 | Testing | 0% ⏳ |
| **Total** | | **~29%** |

---

## Sprint 0 — Multi-tenancy (Core)
**Estado: ✅ Completo**

| Tarea | Estado |
|---|---|
| `TenantContext` (ThreadLocal para schema activo por request) | ✅ |
| `TenantAwareDataSource` (SET search_path en cada getConnection) | ✅ |
| `DataSourceConfig` (@Primary DataSource bean con HikariCP envuelto) | ✅ |
| `TenantFilter` (@Order 1: resuelve tenant desde X-Tenant-ID header) | ✅ |
| Entidad `Tenant` en schema `landlord` (slug, schemaName, email) | ✅ |
| `TenantRepository`, `TenantService`, `TenantServiceImpl` | ✅ |
| `TenantController` con endpoint público `/register` | ✅ |
| Creación automática de schema al registrar tenant (LIKE public.* INCLUDING ALL) | ✅ |
| JWT incluye `tenantSchema` como claim (evita DB lookups en cada request) | ✅ |
| `JwtAuthenticationFilter` setea `TenantContext` desde JWT (sobreescribe header) | ✅ |
| `RevokedToken` movida al schema `landlord` (@Table(schema="landlord")) | ✅ |
| `SecurityConfig` permite `X-Tenant-ID` en CORS + permite `/api/v1/tenants/register` público | ✅ |
| `AuthResponse` incluye `tenantSlug` en la respuesta | ✅ |
| `schema.sql` crea schema `landlord` antes del DDL de Hibernate | ✅ |
| `DataInitializer` refactorizado: crea tenant "polaris" + siembra admin + system values | ✅ |
| Slug al estilo Atlassian (ej: "torres-y-torres") en lugar de RUC | ✅ |

**Avance: 16/16 — 100%**

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
**Estado: ✅ Completo**

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
| Cambio de contraseña (flujo dedicado con verificación) | ✅ |

**Avance: 10/10 — 100%**

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

## Sprint 5 — Catálogos y Productos
**Estado: ✅ Completo**

### SystemValue (Catálogos parametrizables)

| Tarea | Estado |
|---|---|
| Entidad `SystemValue` con soft delete, unique `(catalogType, value)` | ✅ |
| `SystemValueRepository` con `JpaSpecificationExecutor` + queries custom | ✅ |
| `SystemValueSpecification` para filtrado dinámico | ✅ |
| DTOs completos (`CreateSystemValueRequest`, `UpdateSystemValueRequest`, `SystemValueResponse`, `SystemValueFilter`) | ✅ |
| `SystemValueService` (interfaz + implementación) con paginación | ✅ |
| `SystemValueController` con CRUD + endpoint `/catalog/{type}` para dropdowns | ✅ |
| Seed de 20 system values (PRODUCT_TYPE, PRODUCT_TRACKING, UOM, CURRENCY, DOCUMENT_STATUS) | ✅ |

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

**Avance: 8/8 — 100%**

**Avance total Sprint 5: 23/23 — 100%** _(Inventory, Purchases, Sales pasan a sus propios sprints)_

---

## Sprint 6 — Configuración de empresa
**Estado: ⏳ Pendiente**

_Pre-requisito para ventas y compras: la empresa emisora debe existir antes de generar documentos._

### Company (Datos de la empresa emisora)

| Tarea | Estado |
|---|---|
| Entidad `Company` (nombre, RUC, dirección, teléfono, logo) | ❌ |
| `CompanyService` + `CompanyController` (un solo registro por tenant) | ❌ |
| Endpoint `GET /api/v1/company` y `PUT /api/v1/company` | ❌ |

### Warehouse (Bodegas)

| Tarea | Estado |
|---|---|
| Entidad `Warehouse` (nombre, código, dirección, bodega principal) | ❌ |
| `WarehouseService` + `WarehouseController` con CRUD | ❌ |
| Endpoint `GET /api/v1/warehouses` y CRUD completo | ❌ |
| Seed: bodega principal por defecto al crear tenant | ❌ |

### PtoEmision (Puntos de emisión / establecimientos)

| Tarea | Estado |
|---|---|
| Entidad `PtoEmision` (establecimiento, punto de emisión, secuenciales por tipo) | ❌ |
| `PtoEmisionService` + `PtoEmisionController` con CRUD | ❌ |
| Secuenciales de documentos (`Secuencial`): factura, NC, ND, retención, guía | ❌ |

### GeneralSettings (Configuración general del tenant)

| Tarea | Estado |
|---|---|
| Entidad `GeneralSetting` (moneda, zona horaria, idioma, IVA por defecto, logo) | ❌ |
| `GeneralSettingService` + `GeneralSettingController` | ❌ |
| Seed de configuración por defecto al crear tenant | ❌ |

**Avance: 0/12 — 0%**

---

## Sprint 7 — Clientes y Proveedores
**Estado: ⏳ Pendiente**

_Datos maestros de terceros requeridos antes de registrar transacciones._

### Customer (Clientes)

| Tarea | Estado |
|---|---|
| Entidad `Customer` (nombres, RUC/CI, email, teléfono, dirección, tipo persona) | ❌ |
| Entidad `CustomerGroup` (grupos de precio) | ❌ |
| `CustomerRepository` con `JpaSpecificationExecutor` | ❌ |
| `CustomerSpecification` para filtrado dinámico | ❌ |
| DTOs completos + `CustomerService` + `CustomerController` con CRUD paginado | ❌ |
| Soft delete + auditoría | ❌ |
| Seed: cliente "Consumidor Final" por defecto | ❌ |

### Supplier (Proveedores)

| Tarea | Estado |
|---|---|
| Entidad `Supplier` (razón social, RUC, email, teléfono, dirección, condición de pago) | ❌ |
| `SupplierRepository` con `JpaSpecificationExecutor` | ❌ |
| `SupplierSpecification` para filtrado dinámico | ❌ |
| DTOs completos + `SupplierService` + `SupplierController` con CRUD paginado | ❌ |
| Asociación `Product_Supplier` (productos preferidos por proveedor) | ❌ |
| Soft delete + auditoría | ❌ |

**Avance: 0/13 — 0%**

---

## Sprint 8 — Inventario
**Estado: ⏳ Pendiente**

_Gestión de stock: movimientos, Kardex, ajustes, transferencias y conteos físicos._

### Stock por bodega

| Tarea | Estado |
|---|---|
| Entidad `ProductWarehouse` (producto × bodega → stock actual, stock mínimo) | ❌ |
| Inicialización de stock 0 al agregar producto o crear bodega | ❌ |
| Endpoint `GET /api/v1/products/{id}/stock` (stock por bodega) | ❌ |

### Kardex (Historial de movimientos)

| Tarea | Estado |
|---|---|
| Entidad `KardexEntry` (producto, bodega, tipo movimiento, cantidad, stock_antes, stock_despues, referencia, fecha) | ❌ |
| `KardexService` con método `record(product, warehouse, type, qty, ref)` — llamado internamente por todos los módulos | ❌ |
| Endpoint `GET /api/v1/kardex?productId=&warehouseId=&from=&to=` | ❌ |

### Adjustment (Ajustes manuales de inventario)

| Tarea | Estado |
|---|---|
| Entidad `Adjustment` (cabecera: bodega, fecha, motivo, usuario) | ❌ |
| Entidad `AdjustmentLine` (detalle: producto, cantidad, tipo: entrada/salida) | ❌ |
| `AdjustmentService`: aplica cambio de stock + registra en Kardex | ❌ |
| `AdjustmentController` con CRUD | ❌ |

### Transfer (Transferencias entre bodegas)

| Tarea | Estado |
|---|---|
| Entidad `Transfer` (bodega origen, bodega destino, estado: borrador/confirmado) | ❌ |
| Entidad `TransferLine` (producto, cantidad) | ❌ |
| `TransferService`: descuenta origen + agrega destino + registra Kardex en ambas bodegas | ❌ |
| `TransferController` con CRUD + acción confirmar | ❌ |

### StockCount (Conteo físico de inventario)

| Tarea | Estado |
|---|---|
| Entidad `StockCount` (bodega, fecha, estado) | ❌ |
| Entidad `StockCountLine` (producto, stock_sistema, stock_contado, diferencia) | ❌ |
| `StockCountService`: al confirmar → ajusta stock + registra en Kardex | ❌ |
| `StockCountController` con CRUD + acción confirmar | ❌ |

**Avance: 0/17 — 0%**

---

## Sprint 9 — Cotizaciones y Ventas
**Estado: ⏳ Pendiente**

_Núcleo del ERP: el ciclo de venta completo. Requiere Sprints 6, 7 y 8._

### Quotation (Cotizaciones)

| Tarea | Estado |
|---|---|
| Entidad `Quotation` (cliente, fecha, validez, estado: borrador/enviada/aceptada/rechazada) | ❌ |
| Entidad `QuotationLine` (producto, cantidad, precio, descuento, subtotal) | ❌ |
| `QuotationService` + `QuotationController` con CRUD | ❌ |
| Acción: convertir cotización a venta | ❌ |
| DTOs de respuesta con totales calculados (subtotal, IVA, descuentos, total) | ❌ |

### Sale (Ventas)

| Tarea | Estado |
|---|---|
| Entidad `Sale` (cliente, bodega, fecha, tipo: estándar/POS, estado, totales) | ❌ |
| Entidad `SaleLine` (producto, cantidad, precio unitario, IVA, descuento, subtotal) | ❌ |
| Entidad `Payment` (venta, monto, método: efectivo/tarjeta/transferencia, fecha) | ❌ |
| `SaleService`: valida stock → crea Sale + SaleLines → descuenta stock → registra Kardex → registra pago | ❌ |
| `SaleController` con CRUD paginado y filtros (fecha, cliente, estado, bodega) | ❌ |
| Cálculo automático de totales (subtotal, IVA 15%, descuento, total) | ❌ |
| Soporte multi-pago (cash + tarjeta en una misma venta) | ❌ |
| Acción: anular venta (restaura stock + registra en Kardex) | ❌ |
| Estado de pago: pendiente / parcial / pagado | ❌ |
| Endpoint `GET /api/v1/sales/{id}/payments` — historial de pagos | ❌ |
| Endpoint `POST /api/v1/sales/{id}/payments` — registrar abono | ❌ |

**Avance: 0/16 — 0%**

---

## Sprint 10 — Compras
**Estado: ⏳ Pendiente**

_Gestión de compras a proveedores. Requiere Sprints 6, 7 y 8._

### Purchase (Compras)

| Tarea | Estado |
|---|---|
| Entidad `Purchase` (proveedor, bodega, fecha, tipo: factura/liquidación, estado, totales) | ❌ |
| Entidad `PurchaseLine` (producto, cantidad, costo unitario, IVA, subtotal) | ❌ |
| Entidad `PurchasePayment` (compra, monto, método, fecha) | ❌ |
| `PurchaseService`: crea compra → incrementa stock → registra Kardex → registra pago | ❌ |
| `PurchaseController` con CRUD paginado y filtros | ❌ |
| Cálculo de totales (subtotal, IVA, total) | ❌ |
| Estado de pago: pendiente / parcial / pagado | ❌ |
| Acción: anular compra (descuenta stock + registra en Kardex) | ❌ |
| Endpoint `POST /api/v1/purchases/{id}/payments` — registrar abono | ❌ |

### Reception (Recepción de mercadería)

| Tarea | Estado |
|---|---|
| Entidad `Reception` (compra, bodega, fecha, estado) | ❌ |
| Entidad `ReceptionLine` (producto, cantidad esperada, cantidad recibida) | ❌ |
| `ReceptionService`: al confirmar → incrementa stock parcial + registra Kardex | ❌ |
| `ReceptionController` con CRUD + acción confirmar | ❌ |

**Avance: 0/13 — 0%**

---

## Sprint 11 — Devoluciones y Documentos electrónicos
**Estado: ⏳ Pendiente**

_Post-transacciones: correcciones, anulaciones y documentos de ajuste._

### Return (Devoluciones de venta)

| Tarea | Estado |
|---|---|
| Entidad `SaleReturn` (venta original, motivo, fecha, estado) | ❌ |
| Entidad `SaleReturnLine` (producto, cantidad devuelta) | ❌ |
| `SaleReturnService`: restaura stock → registra Kardex → ajusta saldo CxC | ❌ |
| `SaleReturnController` con CRUD | ❌ |

### PurchaseReturn (Devoluciones de compra)

| Tarea | Estado |
|---|---|
| Entidad `PurchaseReturn` (compra original, motivo, fecha) | ❌ |
| Entidad `PurchaseReturnLine` (producto, cantidad devuelta) | ❌ |
| `PurchaseReturnService`: descuenta stock → registra Kardex → ajusta CxP | ❌ |
| `PurchaseReturnController` con CRUD | ❌ |

### NotaCredito / NotaDebito (Documentos de ajuste)

| Tarea | Estado |
|---|---|
| Entidad `CreditNote` (venta/compra referenciada, motivo, monto, estado) | ❌ |
| Entidad `CreditNoteLine` (producto, cantidad, precio) | ❌ |
| Entidad `DebitNote` (referencia, motivo, monto, estado) | ❌ |
| `CreditNoteService` + `DebitNoteService` + controllers | ❌ |

### Retention (Retenciones)

| Tarea | Estado |
|---|---|
| Entidad `Retention` (compra referenciada, fecha, estado) | ❌ |
| Entidad `RetentionLine` (código retención, porcentaje, base imponible, valor) | ❌ |
| Entidad `RetentionCode` (tabla de códigos SRI: FTE e IVA) | ❌ |
| `RetentionService` + `RetentionController` con CRUD | ❌ |
| Seed de códigos de retención estándar (SRI Ecuador) | ❌ |

**Avance: 0/17 — 0%**

---

## Sprint 12 — Caja, Gastos y Contabilidad
**Estado: ⏳ Pendiente**

_Control financiero: movimientos de caja, gastos operativos y contabilidad básica._

### CashRegister (Caja registradora)

| Tarea | Estado |
|---|---|
| Entidad `CashRegister` (punto de emisión, usuario, monto apertura, estado: abierta/cerrada) | ❌ |
| Entidad `CashMovement` (caja, tipo: entrada/salida, monto, concepto) | ❌ |
| `CashRegisterService`: apertura → movimientos durante turno → cierre con cuadre | ❌ |
| `CashRegisterController` con acciones: abrir, cerrar, movimientos | ❌ |

### Expense (Gastos operativos)

| Tarea | Estado |
|---|---|
| Entidad `ExpenseCategory` (categoría de gasto) | ❌ |
| Entidad `Expense` (categoría, monto, fecha, descripción, método de pago) | ❌ |
| `ExpenseService` + `ExpenseController` con CRUD paginado | ❌ |

### Accounting (Contabilidad básica)

| Tarea | Estado |
|---|---|
| Entidad `AccountPlan` (código, nombre, tipo, nivel — plan de cuentas) | ❌ |
| Entidad `AccountingPeriod` (nombre, fecha inicio/fin, estado: abierto/cerrado) | ❌ |
| Entidad `JournalEntry` (período, fecha, descripción, referencia, estado) | ❌ |
| Entidad `JournalLine` (asiento, cuenta, débito, crédito) | ❌ |
| `AccountingService` + controllers con CRUD | ❌ |
| Seed: plan de cuentas básico (activos, pasivos, patrimonio, ingresos, gastos) | ❌ |

**Avance: 0/14 — 0%**

---

## Sprint 13 — Roles y Permisos
**Estado: ⏳ Pendiente**

_Control de acceso granular por módulo dentro del tenant._

| Tarea | Estado |
|---|---|
| Entidad `Role` (nombre, descripción) | ❌ |
| Entidad `Permission` (módulo, acción: create/read/update/delete) | ❌ |
| Tabla pivot `role_permissions` | ❌ |
| Relación `User` → `Role` (ManyToOne) | ❌ |
| Seed: roles por defecto (ADMIN, CASHIER, WAREHOUSE, ACCOUNTANT, VIEWER) | ❌ |
| Seed: permisos por defecto completos | ❌ |
| Anotación o interceptor para verificar permisos por endpoint (`@RequiresPermission`) | ❌ |
| Endpoints `GET/POST/PUT/DELETE /api/v1/roles` | ❌ |
| Endpoint `PUT /api/v1/roles/{id}/permissions` — asignar permisos | ❌ |
| Endpoint `PUT /api/v1/users/{id}/role` — asignar rol a usuario | ❌ |

**Avance: 0/10 — 0%**

---

## Sprint 14 — Promociones
**Estado: ⏳ Pendiente**

_Incentivos de venta: comisiones por vendedor, descuentos, cupones y gift cards._

### Commission (Comisiones)

| Tarea | Estado |
|---|---|
| Entidad `CommissionGroup` (porcentaje de comisión, catálogo de productos aplicables) | ❌ |
| Entidad `Commission` (vendedor, venta, monto base, porcentaje, monto comisión, estado) | ❌ |
| Entidad `CommissionPayment` (registro de pago de comisión al vendedor) | ❌ |
| `CommissionService`: calcula y registra comisión al cerrar venta | ❌ |
| `CommissionController` con CRUD y endpoint de liquidación | ❌ |

### Discount (Descuentos y planes)

| Tarea | Estado |
|---|---|
| Entidad `Discount` (producto/categoría, porcentaje/monto fijo, vigencia) | ❌ |
| Entidad `DiscountPlan` (plan con varios descuentos, asignable a grupo de clientes) | ❌ |
| `DiscountService` + `DiscountController` con CRUD | ❌ |
| Integración con `SaleService`: aplica descuento activo al generar venta | ❌ |

### Coupon (Cupones)

| Tarea | Estado |
|---|---|
| Entidad `Coupon` (código, tipo: porcentaje/monto, límite de usos, vigencia) | ❌ |
| `CouponService`: validar + aplicar cupón en venta | ❌ |
| `CouponController` con CRUD | ❌ |

### GiftCard (Tarjetas de regalo)

| Tarea | Estado |
|---|---|
| Entidad `GiftCard` (código, monto inicial, saldo actual, estado) | ❌ |
| Entidad `GiftCardRecharge` (historial de recargas) | ❌ |
| `GiftCardService`: generar, recargar, consultar saldo, aplicar como método de pago | ❌ |
| `GiftCardController` con CRUD | ❌ |

**Avance: 0/16 — 0%**

---

## Sprint 15 — Logística
**Estado: ⏳ Pendiente**

_Gestión de entregas a domicilio y guías de remisión para transporte._

### Delivery (Entregas)

| Tarea | Estado |
|---|---|
| Entidad `Courier` (nombre, teléfono, vehículo, zona de cobertura) | ❌ |
| Entidad `Delivery` (venta, courier, dirección, estado: pendiente/en_camino/entregado, fecha entrega) | ❌ |
| `DeliveryService` + `DeliveryController` con CRUD y cambio de estado | ❌ |

### GuiaRemision (Guías de remisión)

| Tarea | Estado |
|---|---|
| Entidad `GuiaRemision` (remitente, destinatario, transportista, ruta, fecha traslado) | ❌ |
| Entidad `GuiaRemisionLine` (producto, cantidad, descripción) | ❌ |
| `GuiaRemisionService` + `GuiaRemisionController` con CRUD | ❌ |
| Asociación de guía a venta o transferencia | ❌ |

**Avance: 0/7 — 0%**

---

## Sprint 16 — RRHH y Nómina
**Estado: ⏳ Pendiente**

_Gestión del recurso humano de la empresa. Relativamente independiente del core ERP._

### HR (Recursos Humanos)

| Tarea | Estado |
|---|---|
| Entidad `Department` (nombre, descripción) | ❌ |
| Entidad `Employee` (nombre, cargo, departamento, fecha ingreso, sueldo base, datos IESS) | ❌ |
| `EmployeeService` + `EmployeeController` con CRUD | ❌ |
| Entidad `Attendance` (empleado, fecha, hora entrada, hora salida, horas extras) | ❌ |
| `AttendanceController` con registro manual y resumen mensual | ❌ |
| Entidad `Holiday` (empleado, fecha inicio/fin, tipo, estado: pendiente/aprobado) | ❌ |
| `HolidayController` con solicitud y aprobación de vacaciones | ❌ |

### Payroll (Nómina)

| Tarea | Estado |
|---|---|
| Entidad `Payroll` (período, estado, fecha pago) | ❌ |
| Entidad `PayrollLine` (empleado, sueldo, horas extras, descuentos, neto a pagar) | ❌ |
| `PayrollService`: calcula nómina por período (sueldo + extras - IESS empleado) | ❌ |
| `PayrollController` con CRUD y generación de rol de pagos | ❌ |
| Entidad `SocialBenefit` (décimo tercero, décimo cuarto, fondos de reserva) | ❌ |

**Avance: 0/12 — 0%**

---

## Sprint 17 — Reportes
**Estado: ⏳ Pendiente**

_Reportes analíticos del negocio. Requiere todos los módulos transaccionales._

| Tarea | Estado |
|---|---|
| `GET /api/v1/reports/sales` — ventas por período, bodega, cliente, vendedor | ❌ |
| `GET /api/v1/reports/purchases` — compras por período, proveedor | ❌ |
| `GET /api/v1/reports/profit-loss` — P&L: ingresos − COGS − gastos | ❌ |
| `GET /api/v1/reports/stock` — stock actual por producto y bodega | ❌ |
| `GET /api/v1/reports/stock-alerts` — productos bajo stock mínimo | ❌ |
| `GET /api/v1/reports/kardex` — Kardex detallado con filtros | ❌ |
| `GET /api/v1/reports/cxc` — cuentas por cobrar con saldo pendiente por cliente | ❌ |
| `GET /api/v1/reports/cxp` — cuentas por pagar con saldo pendiente por proveedor | ❌ |
| `GET /api/v1/reports/top-products` — productos más vendidos | ❌ |
| `GET /api/v1/reports/top-customers` — mejores clientes por facturación | ❌ |
| `GET /api/v1/reports/cash-register` — resumen de cierre de caja | ❌ |

**Avance: 0/11 — 0%**

---

## Sprint 18 — Integraciones externas
**Estado: ⏳ Pendiente (opcional / fase 2)**

_Módulos de integración con servicios de terceros. Baja prioridad para MVP._

| Tarea | Estado |
|---|---|
| Integración WhatsApp: enviar facturas/cotizaciones por mensaje | ❌ |
| Integración WooCommerce: sincronizar productos y órdenes | ❌ |
| Webhook SRI: recepción de documentos electrónicos del proveedor | ❌ |
| SMTP configurable por tenant (mail setting) | ❌ |
| Envío de documentos por email (ventas, compras, cotizaciones) | ❌ |

**Avance: 0/5 — 0%**

---

## Sprint 19 — Testing
**Estado: ⏳ Pendiente**

| Tarea | Estado |
|---|---|
| Tests unitarios para `UserServiceImpl` | ❌ |
| Tests unitarios para `AuthServiceImpl` (login, refresh, logout) | ❌ |
| Tests unitarios para `SystemValueServiceImpl` | ❌ |
| Tests unitarios para `ProductServiceImpl` | ❌ |
| Tests unitarios para `ProductCategoryServiceImpl` | ❌ |
| Tests unitarios para `SaleServiceImpl` | ❌ |
| Tests unitarios para `PurchaseServiceImpl` | ❌ |
| Tests unitarios para `KardexService` | ❌ |
| Tests unitarios para `JwtService` | ❌ |
| Tests de integración para `AuthController` | ❌ |
| Tests de integración para `UserController` | ❌ |
| Tests de integración para `SaleController` | ❌ |
| Tests de integración para `ProductController` | ❌ |
| Configurar Testcontainers para PostgreSQL en tests de integración | ❌ |

**Avance: 0/14 — 0%**

---

## Endpoints implementados

| Método | Ruta | Implementado | Protegido con JWT |
|---|---|---|---|
| POST | `/api/v1/auth/login` | ✅ | N/A (pública) |
| POST | `/api/v1/auth/refresh` | ✅ | N/A (pública) |
| POST | `/api/v1/auth/logout` | ✅ | ✅ |
| POST | `/api/v1/tenants/register` | ✅ | N/A (pública) |
| GET | `/api/v1/tenants` | ✅ | ✅ |
| POST | `/api/v1/users` | ✅ | ✅ |
| GET | `/api/v1/users` | ✅ | ✅ |
| GET | `/api/v1/users/{id}` | ✅ | ✅ |
| PATCH | `/api/v1/users/{id}` | ✅ | ✅ |
| DELETE | `/api/v1/users/{id}` | ✅ | ✅ |
| POST | `/api/v1/systemvalues` | ✅ | ✅ |
| GET | `/api/v1/systemvalues` | ✅ | ✅ |
| GET | `/api/v1/systemvalues/{id}` | ✅ | ✅ |
| GET | `/api/v1/systemvalues/catalog/{type}` | ✅ | ✅ |
| PATCH | `/api/v1/systemvalues/{id}` | ✅ | ✅ |
| DELETE | `/api/v1/systemvalues/{id}` | ✅ | ✅ |
| POST | `/api/v1/product-categories` | ✅ | ✅ |
| GET | `/api/v1/product-categories` | ✅ | ✅ |
| GET | `/api/v1/product-categories/{id}` | ✅ | ✅ |
| GET | `/api/v1/product-categories/roots` | ✅ | ✅ |
| GET | `/api/v1/product-categories/{id}/children` | ✅ | ✅ |
| PATCH | `/api/v1/product-categories/{id}` | ✅ | ✅ |
| DELETE | `/api/v1/product-categories/{id}` | ✅ | ✅ |
| POST | `/api/v1/products` | ✅ | ✅ |
| GET | `/api/v1/products` | ✅ | ✅ |
| GET | `/api/v1/products/{id}` | ✅ | ✅ |
| PATCH | `/api/v1/products/{id}` | ✅ | ✅ |
| DELETE | `/api/v1/products/{id}` | ✅ | ✅ |

---

## Orden de dependencias (resumen)

```
Sprint 0 (Multi-tenancy)
    └─► Sprint 1 (Infra) → Sprint 3 (JWT) → Sprint 4 (Errores)
            └─► Sprint 2 (Users)
                    └─► Sprint 5 (Catálogos + Productos)
                            └─► Sprint 6 (Empresa + Bodegas + Config)
                                    └─► Sprint 7 (Clientes + Proveedores)
                                            └─► Sprint 8 (Inventario + Kardex)
                                                    ├─► Sprint 9 (Cotizaciones + Ventas)
                                                    └─► Sprint 10 (Compras)
                                                            └─► Sprint 11 (Devoluciones + NC/ND + Retenciones)
                                                                    └─► Sprint 12 (Caja + Gastos + Contabilidad)
                                                                    └─► Sprint 13 (Roles + Permisos)
                                                                    └─► Sprint 14 (Comisiones + Descuentos + Gift Cards)
                                                                    └─► Sprint 15 (Delivery + Guías)
                                                                    └─► Sprint 16 (RRHH + Nómina)
                                                                            └─► Sprint 17 (Reportes)
                                                                                    └─► Sprint 18 (Integraciones)
                                                                                            └─► Sprint 19 (Testing)
```

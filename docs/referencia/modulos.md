# Módulos del Sistema (Tenant)

Todos los módulos del tenant viven en `routes/tenant.php` y sus vistas en `resources/views/backend/`.

---

## 1. Ventas (Sales)

**Controlador:** `SaleController`
**Modelo principal:** `Sale`

Gestiona el ciclo completo de ventas:
- Creación de venta estándar, venta POS (Point of Sale) y venta por CSV
- Múltiples formas de pago: efectivo, cheque, tarjeta de crédito, PayPal, gift card, multipago
- Aplicación de cupones y descuentos
- Generación de factura electrónica (XML + PDF) al crear/editar una venta
- Envío de factura por email o WhatsApp
- Anulación de ventas con nota de crédito
- Gestión de pagos parciales y abonos
- Integración con WooCommerce (`woocommerce_order_id`)
- Soporte para mesa/hall (módulo restaurante), vendedor asignado y punto de emisión

**Campos clave del modelo `Sale`:**
- `clave_acceso`, `estado_fact_sri`, `mensaje_sri` → estado en el SRI
- `tipo_documento`, `tipoComprobante` → tipo de comprobante SRI
- `ambiente` → pruebas (1) o producción (2)
- `biller_id` → punto de emisión (`PtoEmision`)
- `seller_id` → vendedor asignado

---

## 2. Compras (Purchases)

**Controlador:** `PurchaseController`
**Modelo principal:** `Purchase`

- Registro de compras a proveedores con productos y variantes
- Liquidaciones de compra (tipo 03 SRI) para compras sin factura de proveedor
- Generación de XML de liquidación y retención automática en compra
- Importación masiva por CSV
- Pagos parciales y seguimiento de saldo pendiente
- Devoluciones de compra (`ReturnPurchaseController`)
- Recepciones de mercadería (`RecepcionController`, `DetailReception`)

---

## 3. Cotizaciones (Quotations)

**Controlador:** `QuotationController`
**Modelo principal:** `Quotation`

- Generación de cotizaciones con productos y variantes
- Conversión de cotización a venta
- Envío por email

---

## 4. Devoluciones (Returns)

**Controladores:** `ReturnController` (venta), `ReturnPurchaseController` (compra)
**Modelos:** `Returns`, `ProductReturn`, `ReturnPurchase`, `PurchaseProductReturn`

- Devolución parcial o total de ventas
- Generación de nota de crédito electrónica al devolver
- Restauración de stock en bodega

---

## 5. Productos e Inventario

**Controlador:** `ProductController`
**Modelos:** `Product`, `ProductVariant`, `Product_Warehouse`, `ProductBatch`, `PriceList`, `ProductImage`

- CRUD de productos con variantes, unidades, categorías y marcas
- Gestión de stock por bodega (`Product_Warehouse`)
- Control de lotes/batches (`ProductBatch`) y fechas de expiración
- Listas de precios (`PriceList`) por cliente/grupo
- Impresión de código de barras
- Historial de movimientos por producto
- Productos digitales y combo
- Asignación de productos a vendedor (`SellerController`)

**Controlador:** `AdjustmentController`
Ajustes manuales de inventario (entrada/salida con motivo).

**Controlador:** `TransferController`
Transferencias de stock entre bodegas.

**Controlador:** `StockCountController`
Conteo físico de inventario (stocktake).

**Controlador:** `KardexController`
Kardex de movimientos de inventario por producto y bodega.

---

## 6. Clientes

**Controlador:** `CustomerController`
**Modelos:** `Customer`, `CustomerGroup`, `CustomerAdress`, `CustomerExtra`, `CustomerRetention`, `CxCBalances`

- CRUD de clientes con información tributaria (RUC/CI)
- Grupos de clientes con precios diferenciados
- Saldo de anticipos y depósitos
- Cuentas por cobrar (CxC) y retenciones de clientes
- Puntos de recompensa (`RewardPointSetting`)

---

## 7. Proveedores

**Controlador:** `SupplierController`
**Modelos:** `Supplier`, `Product_Supplier`, `CxPBalances`

- CRUD de proveedores
- Asociación con productos
- Cuentas por pagar (CxP)

---

## 8. Notas de Crédito y Débito

**Controlador:** `DocumentosElectController`
**Modelos:** `NotaCredito`, `ProductNotaCredito`, `NotaDebito`

- Creación de notas de crédito por devoluciones o ajustes
- Notas de débito para cobros adicionales
- Generación de XML firmado y envío al SRI
- Descarga de XML y PDF
- Reenvío de documentos por email
- Gestión de documentos recibidos desde el SRI (compras electrónicas)

---

## 9. Guías de Remisión

**Controlador:** `GuiaController`
**Modelos:** `Guia`, `DetalleGuia`

- Creación de guías de remisión para transporte de mercadería
- Generación de XML tipo 06
- Gestión de transportistas

---

## 10. Retenciones

**Controlador:** `RetencionController`
**Modelos:** `Retencion`, `DetalleRetencion`, `RetentionCode`, `RetentionPercentage`, `CodigosRetencion`

- Retenciones en la fuente (FTE) y retenciones IVA
- Generación de XML tipo 07
- Tabla de códigos de retención con porcentajes
- Importación de tablas de FTE

---

## 11. Contabilidad

**Controlador:** `AccountingController`
**Modelos:** `AccountPlan`, `AccountSettings`, `AccountingPeriod`, `JournalEntry`, `JournalGeneral`, `TypeAccountingJournal`, `TypeAccount`

- Plan de cuentas configurable
- Períodos contables (abrir/cerrar)
- Asientos de diario manuales y automáticos (generados por ventas/compras/pagos)
- Balance general y estados financieros
- Importación de saldos de apertura
- Exportación a Excel

**Controlador:** `AccountsController`
**Modelo:** `Account`
Cuentas bancarias/cajas del tenant (cuentas de tesorería).

**Controlador:** `BankController`
**Modelo:** `Bank`, `CuentaBancaria`, `CashBank`
Bancos, cuentas bancarias y conciliación de movimientos de caja/banco.

**Controlador:** `CashRegisterController`
**Modelo:** `CashRegister`
Cajas registradoras: apertura, cierre, movimientos de entrada/salida.

---

## 12. Gastos

**Controladores:** `ExpenseController`, `ExpenseCategoryController`
**Modelos:** `Expense`, `ExpenseCategory`

- Registro de gastos operativos por categoría
- Afectación a cuentas contables

---

## 13. Recursos Humanos (RRHH)

**Controlador:** `EmployeeController`
**Modelos:** `Employee`, `CargoEmpleado`, `Jornada`, `datos_adicionalesemp`, `cargas_familiares`

Gestión de empleados con datos personales, cargos y jornada laboral.

**Controlador:** `DepartmentController`
**Modelo:** `Department`
Estructura organizacional por departamentos.

**Controlador:** `AttendanceController`
**Modelos:** `Attendance`, `asistencia_biometrico`, `calculo_horas`
Registro de asistencia manual y biométrico.

**Controlador:** `HolidayController`
**Modelo:** `Holiday`
Solicitudes y aprobación de vacaciones con email de notificación.

---

## 14. Nómina (Payroll)

**Controlador:** `PayrollController`
**Modelos:** `Payroll`, `cabnomina`, `detnomina`, `BeneficiosSociales`, `HistSueldoBasico`, `det_prestamos`, `cargos_judiciales`

- Cálculo de nómina por período
- Beneficios sociales (décimos, fondos de reserva, liquidaciones)
- Préstamos a empleados
- Cargos judiciales (retenciones judiciales)
- Historial de sueldo básico

---

## 15. Comisiones

**Controlador:** `CommissionController`
**Modelos:** `Commission`, `CommissionGroup`, `CommissionLeader`, `CommissionApply`, `CommissionPayment`

- Grupos de comisión por vendedor o líderes
- Aplicación de comisiones a ventas
- Registro de pagos de comisiones

---

## 16. Descuentos y Cupones

**Controladores:** `DiscountController`, `DiscountPlanController`, `CouponController`
**Modelos:** `Discount`, `DiscountPlan`, `DiscountPlanCustomer`, `Coupon`

- Descuentos por producto o plan
- Planes de descuento por cliente
- Cupones de descuento con validación en el POS/venta

---

## 17. Gift Cards

**Controlador:** `GiftCardController`
**Modelos:** `GiftCard`, `GiftCardRecharge`, `PaymentWithGiftCard`

- Generación y recarga de gift cards
- Uso en ventas como método de pago

---

## 18. Delivery y Courier

**Controladores:** `DeliveryController`, `CourierController`
**Modelos:** `Delivery`, `Courier`

- Gestión de envíos y repartidores
- Asignación de ventas a courier

---

## 19. Bodegas y Configuración

**Controlador:** `WarehouseController`
**Modelo:** `Warehouse`, `Storage`
Bodegas del tenant con gestión de stock independiente.

**Controlador:** `CompanyController`
**Modelo:** `Company`
Datos de la empresa emisora: RUC, razón social, certificado P12, ambiente SRI, punto de emisión.

**Controlador:** `PtoEmisionController`
**Modelo:** `PtoEmision`, `Secuencial`
Puntos de emisión (establecimientos) y secuenciales de documentos.

---

## 20. Configuración General

**Controlador:** `SettingController`
**Modelos:** `GeneralSetting`, `MailSetting`, `PosSetting`, `HrmSetting`, `ParametrosGenerales`

- Configuración de moneda, impuestos, fecha, tema
- Configuración de email (SMTP)
- Configuración del POS
- Parámetros generales del negocio

---

## 21. Reportes

**Controlador:** `ReportController`

- Reporte de ventas / compras por período y bodega
- Reporte de productos (stock, movimientos)
- Profit & Loss
- Mejores vendedores
- Reporte de CxC (cuentas por cobrar) con PDF
- Reporte de bodega (entradas/salidas)
- Alertas de stock mínimo y productos próximos a vencer

---

## 22. E-Commerce (Shop)

**Controlador:** `StoreController`
**Modelos:** `ShopOrders`, `ShopPayment`, `WishList`, `DropshipRequest`

- Tienda en línea integrada al tenant (`/my_shop`)
- Carrito de compras, wish list
- Login/registro de clientes en la tienda
- Dropshipping
- Personalización de productos (`LumiseController`, `CustomizerProduct`)

---

## 23. Usuarios y Roles

**Controladores:** `UserController`, `RoleController`
**Modelos:** `User`, `Roles`, `Permission`

- CRUD de usuarios del tenant
- Roles con asignación de permisos granular por módulo
- Los permisos disponibles están limitados por el paquete SaaS contratado

---

## 24. WhatsApp e Integraciones Externas

**Controladores:** `WhatsController`, `ApiFactucampo`, `ApiChateamController`, `SmartTrackController`

- Envío de documentos (facturas, cotizaciones) por WhatsApp
- Integración con API de WhatsApp (sesión QR, mensajes, archivos)
- Webhook para recepción de mensajes

---

## 25. Módulos Adicionales (nwidart/laravel-modules)

Módulos opcionales activables según el paquete contratado:
- **woocommerce**: Sincronización con WooCommerce
- **ecommerce**: Módulo e-commerce extendido

Los módulos se registran en `config/modules.php` y se activan/desactivan desde el landlord.

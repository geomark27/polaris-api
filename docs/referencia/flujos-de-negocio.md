# Flujos de Negocio

## 1. Ciclo Completo de Venta con Factura Electrónica

```
Usuario crea venta
       │
       ▼
SaleController::store(Request $request)
       │
       ├─ Valida stock disponible por bodega (Product_Warehouse)
       ├─ Calcula totales: subtotal + impuestos + descuentos + shipping
       ├─ Aplica cupón si existe (Coupon::validate())
       ├─ Aplica gift card si existe
       ├─ Crea registro Sale en DB
       ├─ Crea registros Product_Sale (líneas de detalle)
       ├─ Descuenta stock en Product_Warehouse
       ├─ Registra pago(s) en Payment / PaymentWithCheque / etc.
       ├─ Actualiza saldo en Account (caja/banco)
       ├─ Genera asiento contable (JournalEntry) si contabilidad activa
       │
       ▼
SaleController::crearFacturaXml($id)
       │
       ├─ PlansVerifyController::VerifyPlan() → verifica plan vigente
       ├─ Obtiene datos de Company (emisor) y Customer (receptor)
       ├─ Construye XML via app/FacturacionElectronica/Factura/
       ├─ Guarda XML en storage/{ruc}/
       │
       ▼
ProcesarDocumentoService::procesarComprobante($data)
       │
       ├─ validarEmisor() → Company existe y tiene firma configurada
       ├─ FacturacionElectronica::firmarXml() → firma con P12
       ├─ FacturacionElectronicaService::enviarComprobanteSri() → SOAP validación
       ├─ FacturacionElectronicaService::autorizarComprobanteSri() → SOAP autorización
       └─ Sale::update(['mensaje_sri' => $respuesta, 'estado_fact_sri' => 'AUTORIZADO'])
```

**Anulación de venta:**
- `SaleController::anularSale()` → cambia `sale_status = 'anulado'`
- Puede generar una Nota de Crédito automáticamente para el SRI
- Restaura stock en bodega

---

## 2. Ciclo de Compra con Retención

```
Usuario registra compra
       │
       ▼
PurchaseController::store(Request $request)
       │
       ├─ Valida y crea Purchase en DB
       ├─ Crea ProductPurchase (líneas)
       ├─ Incrementa stock en Product_Warehouse
       ├─ Si Company::auto_retention = true → genera Retencion automáticamente
       │
       ▼ (si es liquidación de compra)
PurchaseController::generarXml($id)    → XML tipo 03
PurchaseController::generarRetentionXML($id)  → XML tipo 07
       │
       ▼
ProcesarDocumentoService::procesarComprobante()
       └─ Mismo flujo: firmar → validar SRI → autorizar SRI
```

**Recepción de documentos electrónicos del proveedor:**
```
Proveedor envía XML al SRI → SRI notifica al receptor
DocumentosElectController::receivedIndex() → lista docs recibidos
DocumentosElectController::insertPurchase($docs) → crea Purchase desde XML
DocumentosElectController::insertRetention($docs) → crea Retencion desde XML
```

---

## 3. Flujo de Nota de Crédito

```
Motivo: devolución de venta o ajuste de precio
       │
       ▼
DocumentosElectController::notaCreditoCreate()
       ├─ Selecciona la venta original (Sale)
       ├─ Selecciona productos/cantidades a devolver
       ├─ Crea NotaCredito con referencia a Sale::clave_acceso
       ├─ Crea ProductNotaCredito (detalle)
       ├─ Restaura stock en Product_Warehouse
       │
       ▼
DocumentosElectController::generarNotaCreditoXml($id)
       └─ XML tipo 04 → firmar → enviar SRI → autorizar
```

---

## 4. Gestión de Cotización → Venta

```
QuotationController::store()
       └─ Crea Quotation + ProductQuotation

Cliente aprueba cotización
       │
       ▼
QuotationController → convierte a Sale
       └─ Copia datos de Quotation a Sale
       └─ Descuenta stock
       └─ Genera factura electrónica (mismo flujo que venta)
```

---

## 5. Proceso de Nómina

```
PayrollController::store()
       │
       ├─ Selecciona período y empleados activos
       ├─ Calcula: sueldo + horas extras - descuentos - cargas judiciales
       ├─ Aplica aportes IESS (empleado y patronal)
       ├─ Calcula beneficios sociales si aplica (décimo tercer sueldo, etc.)
       ├─ Crea cabnomina (cabecera) + detnomina (detalle por empleado)
       └─ Genera rol de pagos en PDF para cada empleado
```

---

## 6. Flujo del Kardex de Inventario

El Kardex (`KardexController`) registra automáticamente cada movimiento de inventario:

| Evento | Tipo movimiento | Controlador origen |
|---|---|---|
| Venta creada | Salida | `SaleController::store()` |
| Venta anulada | Entrada (reversión) | `SaleController::anularSale()` |
| Compra creada | Entrada | `PurchaseController::store()` |
| Compra anulada | Salida (reversión) | `PurchaseController::anular()` |
| Devolución venta | Entrada | `ReturnController::store()` |
| Devolución compra | Salida | `ReturnPurchaseController::store()` |
| Transferencia | Salida/Entrada por bodega | `TransferController::store()` |
| Ajuste de inventario | Entrada o Salida | `AdjustmentController::store()` |
| Conteo físico | Ajuste | `StockCountController` |

---

## 7. Gestión de Caja (Cash Register)

```
CashRegisterController::open()
       └─ Crea apertura de caja con monto inicial

Durante el turno:
       ├─ Ventas → suman a la caja
       ├─ CashBankIncomes → entradas manuales
       └─ CashBankOutcomes → salidas manuales

CashRegisterController::close()
       ├─ Calcula total teórico vs. contado físico
       ├─ Registra diferencia
       └─ Genera reporte de cierre de caja
```

---

## 8. Flujo de Entrega (Delivery)

```
Venta creada
       │
       ▼
DeliveryController::store()
       ├─ Asigna courier/repartidor a la venta
       ├─ Registra dirección de entrega
       └─ Estado: pendiente → en camino → entregado

GuiaController::store() (si requiere guía de remisión)
       └─ Genera XML tipo 06 para el transporte
```

---

## 9. Reportes Financieros

```
ReportController::profitLoss()
       ├─ Suma de ventas del período (Sale::grand_total)
       ├─ Menos costo de ventas (ProductSale × costo promedio)
       ├─ Menos gastos del período (Expense)
       └─ Resultado: utilidad/pérdida bruta y neta

ReportController::cxcReport()
       ├─ Sale con payment_status != 'paid'
       ├─ Calcula saldo pendiente por cliente
       └─ Exporta a PDF (DIAN-style)
```

---

## 10. Ciclo de Vida de la Suscripción Tenant

Ver también: [saas-landlord.md](saas-landlord.md)

```
Acceso del tenant en cada request
       │
       ▼
Middleware Common::handle()
       ├─ Lee general_settings::expiry_date del tenant
       ├─ Si expiró → auth()->logout() + redirect a CENTRAL_DOMAIN/contact-for-renewal
       └─ Si vigente → continúa normalmente

Renovación automática (Stripe webhook):
StripeWebhookController::handleWebhook()
       └─ Actualiza expiry_date en general_settings del tenant
```

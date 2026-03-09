# Facturación Electrónica (SRI Ecuador)

## Tipos de Comprobantes Soportados

| Código SRI | Tipo | Controlador/Método | Modelo |
|---|---|---|---|
| 01 | Factura de venta | `SaleController::crearFacturaXml()` | `Sale` |
| 03 | Liquidación de compra | `PurchaseController::generarXml()` | `Purchase` |
| 04 | Nota de crédito | `DocumentosElectController::generarNotaCreditoXml()` | `NotaCredito` |
| 05 | Nota de débito | `DocumentosElectController::InsertarNotaDebito()` | `NotaDebito` |
| 06 | Guía de remisión | `GuiaController::generarGuiaXml()` | `Guia` |
| 07 | Comprobante de retención | `PurchaseController::generarRetentionXML()` | `Retencion` |

---

## Flujo de un Comprobante Electrónico

```
1. GENERAR XML
   └─ Controlador llama al builder en app/FacturacionElectronica/
   └─ Se construye el XML con datos del documento + emisor
   └─ XML se guarda en storage/ con nombre = clave_acceso

2. FIRMAR XML
   └─ ProcesarDocumentoService::procesarComprobante()
   └─ FacturacionElectronica::firmarXml($claveFirma, $firma, $archivo, $ruc)
   └─ Usa RobRichards/XMLSecLibs con certificado P12 de la empresa
   └─ Resultado: XML firmado en disco

3. VALIDAR EN SRI
   └─ FacturacionElectronicaService::enviarComprobanteSri($xml, $ambiente)
   └─ SOAP → RecepcionComprobantesOffline?wsdl
   └─ SRI responde: RECIBIDA / DEVUELTA + mensajes de error

4. AUTORIZAR EN SRI
   └─ FacturacionElectronicaService::autorizarComprobanteSri($claveAcceso, $ambiente)
   └─ SOAP → AutorizacionComprobantesOffline?wsdl
   └─ SRI responde: AUTORIZADO / NO AUTORIZADO + número de autorización

5. ACTUALIZAR DOCUMENTO
   └─ Se guarda mensaje_sri en el modelo
   └─ Se registra estado_fact_sri y fecha_autorizacion

6. NOTIFICAR
   └─ Envío de PDF + XML al cliente por email (opcional)
   └─ Envío por WhatsApp (opcional)
```

---

## Clave de Acceso

La clave de acceso es un número de 49 dígitos que identifica unívocamente cada comprobante ante el SRI:

```
[fecha(8)][tipoComp(2)][ruc(13)][ambiente(1)][serie(6)][secuencial(9)][codigoNumerico(8)][tipoEmision(1)][dv(1)]
```

Se almacena en el campo `clave_acceso` del modelo correspondiente y se usa como nombre del archivo XML.

---

## Configuración de la Empresa Emisora

El modelo `Company` (`app/Models/Company.php`) contiene toda la configuración del emisor:

| Campo | Descripción |
|---|---|
| `ruc` | RUC del emisor (usado como ID del tenant en muchos casos) |
| `razon_social` | Nombre legal |
| `nombre_comercial` | Nombre comercial |
| `ambiente` | `1` = pruebas, `2` = producción |
| `tipo_emision` | `1` = normal |
| `firma` | Ruta al archivo `.p12` del certificado digital |
| `password_firma` | Contraseña del certificado P12 |
| `limitDate` | `1` si el certificado está vigente |
| `validTo_time_t` | Timestamp de vencimiento del certificado |
| `agente_retencion` | Si aplica retención en todas las compras |
| `auto_advance` | Avance automático de documentos |
| `auto_purchase` | Genera automáticamente retención al registrar compra |
| `auto_retention` | Genera retención automática |
| `contribuyente` | Tipo de contribuyente |
| `obligado_contabilidad` | Si está obligado a llevar contabilidad |
| `regimen` | Régimen tributario |

---

## Puntos de Emisión (`PtoEmision`)

Cada empresa puede tener múltiples establecimientos (puntos de emisión). El modelo `PtoEmision` contiene:
- `establecimiento` + `punto_emision` → forman la serie del documento (ej: `001-001`)
- Se asocia a una `Sale` mediante `biller_id`
- Los secuenciales se gestionan en el modelo `Secuencial` y `SecuencialFactura`

---

## Ambientes SRI

### Pruebas
- Validación: `https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline?wsdl`
- Autorización: `https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline?wsdl`

### Producción
- Validación: `https://cel.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline?wsdl`
- Autorización: `https://cel.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline?wsdl`

El ambiente se determina desde `Company::ambiente` y se verifica en `PlansVerifyController::VerifyPlan()`.

---

## Constructores XML (`app/FacturacionElectronica/`)

Cada tipo de comprobante tiene su propio builder:

### Factura (Tipo 01) — `Factura/`
Genera el XML con:
- `InfoTributaria` → datos del emisor (RUC, razón social, etc.)
- `infoFactura` → datos del comprador, fecha, totales, forma de pago
- `detalles` → líneas de productos con impuestos
- `infoAdicional` → campos adicionales opcionales
- `Pagos` → formas de pago aceptadas por SRI

### Liquidación de Compra (Tipo 03) — `liquidacion/`
Para compras a personas naturales sin RUC que no emiten facturas.
- Usa `InfoLiquidacionCompra` y `Liquidacion`

### Nota de Crédito (Tipo 04) — `nota_credito/`
- Referencia al documento que modifica (`docModificado`)
- `InfoNotaCredito` con razón de devolución/ajuste
- Detalle de productos devueltos

### Nota de Débito (Tipo 05) — `nota_debito/`
- `InfoNotaDebito` con razón del cobro adicional
- Modelo `Motivo` para los motivos del débito

### Guía de Remisión (Tipo 06) — `GuiaController`
- Datos del transportista y destinos
- Detalle de mercadería transportada
- `DetalleGuia` con los productos

### Retención (Tipo 07) — `RetencionController`, `PurchaseController`
- `DetalleRetencion` con código, porcentaje y base imponible
- Tabla de códigos: `CodigosRetencion`, `RetentionCode`, `RetentionPercentage`

---

## Firma Digital

**Librería:** `RobRichards/XMLSecLibs`

El proceso en `FacturacionElectronica::firmarXml()`:
1. Lee el XML del storage
2. Sanitiza caracteres especiales (tildes → sin tildes; SRI no acepta UTF-8 con acentos)
3. Carga el certificado P12 con `openssl_pkcs12_read()`
4. Firma el documento con `XMLSecurityDSig` usando la clave privada del certificado
5. Guarda el XML firmado en disco

---

## Verificación del Plan antes de Emitir

`PlansVerifyController::VerifyPlan()` valida antes de enviar al SRI:
1. Que la fecha actual esté dentro del período de plan (`fechaInicio` → `feFin`)
2. Que no se haya alcanzado el límite de comprobantes del plan (`cantComprobante`)
3. Que el certificado digital esté vigente (`limitDate === 1`)

---

## Documentos Recibidos desde el SRI

El sistema también puede **recibir** comprobantes electrónicos de proveedores:
- `DocumentosElectController::receivedIndex()` → lista documentos recibidos
- `DocumentosElectController::insertPurchase()` → convierte doc SRI en compra interna
- `DocumentosElectController::insertRetention()` → convierte retención SRI en retención interna
- `DocumentosElectController::insertDocBatch()` → importación masiva de documentos XML

---

## Descarga y Envío

- `XmlDownload($doc_id)` → descarga el XML firmado del storage
- `PDFGenerate($doc_id)` → genera PDF de la representación impresa
- `MailResend($request)` → reenvía email con XML + PDF al cliente
- `MassiveDownload()` / `SimpleDownload()` → descarga ZIP de múltiples documentos

---

## Estado del Documento en el Modelo `Sale`

| Campo | Descripción |
|---|---|
| `estado_fact_sri` | Estado actual: `PENDIENTE`, `RECIBIDA`, `AUTORIZADO`, `NO AUTORIZADO`, `ANULADO` |
| `mensaje_sri` | Respuesta textual del SRI (JSON serializado) |
| `clave_acceso` | Clave de 49 dígitos del comprobante |
| `fecha_autorizacion` | Fecha/hora de autorización del SRI |
| `numero_documento` | Número secuencial del comprobante |
| `ambiente` | 1=pruebas, 2=producción |

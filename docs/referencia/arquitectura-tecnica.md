# Arquitectura Técnica

## Stack Tecnológico

| Capa | Tecnología |
|---|---|
| Backend | PHP / Laravel |
| Base de datos | MySQL (dos instancias) |
| Servidor | Apache (XAMPP en Windows) |
| Multi-tenancy | [Stancl/Tenancy](https://tenancyforlaravel.com/) |
| Roles y permisos | Spatie Laravel-Permission |
| Módulos opcionales | nwidart/laravel-modules |
| Firma digital XML | RobRichards/XMLSecLibs |
| Frontend | Blade + JS (sin SPA) |

---

## Arquitectura de Bases de Datos

El sistema usa **dos bases de datos MySQL completamente separadas**:

```
┌───────────────────────────────────┐   ┌────────────────────────────────────┐
│         LANDLORD DB               │   │           TENANT DB                │
│   conexión: saleprosaas_landlord  │   │   conexión: saleprosaas_tenant     │
│   env: LANDLORD_DB                │   │   (dinámica por dominio)           │
│                                   │   │                                    │
│  - tenants / domains              │   │  - users / roles / permissions     │
│  - packages / subscriptions       │   │  - sales / purchases               │
│  - general_settings (landlord)    │   │  - products / inventory            │
│  - users (super admin)            │   │  - customers / suppliers           │
│  - payments / failed_jobs         │   │  - companies / settings            │
│  - landing page content           │   │  - electronic documents            │
│  - modules / features / heroes    │   │  - accounting / payroll            │
└───────────────────────────────────┘   └────────────────────────────────────┘
```

### Conexiones definidas en `config/database.php`

- `saleprosaas_landlord` → base de datos central (env `LANDLORD_DB`)
- `saleprosaas_tenant` → plantilla dinámica para bases de datos de tenants
- El paquete Stancl/Tenancy inicializa la conexión del tenant activo basándose en el dominio de la request

### Resolución del Tenant

La inicialización ocurre vía middleware `InitializeTenancyByDomain` en `routes/tenant.php`. El dominio de la request se compara contra la tabla `domains` en la DB landlord. Los dominios centrales (`localhost`, `127.0.0.1`, `CENTRAL_DOMAIN`) están excluidos de la tenancy.

---

## Estructura de Rutas

```
routes/
├── web.php           → Rutas landlord/centrales (super admin, landing, pagos SaaS)
├── tenant.php        → Rutas tenant (negocio, envueltas en InitializeTenancyByDomain)
├── api.php           → Webhooks SRI, WhatsApp, móvil
└── mobile_api.php    → API para app móvil
```

### Grupos de middleware clave en `tenant.php`

```php
Route::group(['middleware' => ['web', InitializeTenancyByDomain::class, PreventAccessFromCentralDomains::class]], function () {
    // Rutas de setup sin auth
    // Rutas con auth básica
    Route::group(['middleware' => ['common', 'auth', 'active']], function () {
        // Todo el negocio del tenant
    });
});
```

---

## Middleware

| Middleware | Función |
|---|---|
| `SuperAdminAuth` | Protege rutas del super admin landlord; lee configuración global y la pasa a la vista |
| `Common` | Ejecuta en cada request tenant: verifica expiración de suscripción, comparte `general_setting` y `subdomain` con vistas, valida existencia de `external_services` |
| `Active` | Verifica que el usuario tenant esté activo |
| `Cors` | Agrega header `Access-Control-Allow-Origin: *` |
| `VerifySetupToken` | Protege endpoints de instalación/setup |
| `InitializeTenancyByDomain` | (Stancl) Cambia la conexión DB al tenant correspondiente |
| `PreventAccessFromCentralDomains` | (Stancl) Bloquea acceso a rutas tenant desde dominio central |

---

## Autenticación

### Super Admin (Landlord)
- Login en `/` → `Auth\SuperAdminLoginController`
- Usa la tabla `users` de la DB landlord
- Protegido por middleware `SuperAdminAuth`
- Soporte para `multi_access`: permite al super admin entrar a una sesión de tenant específico

### Usuarios Tenant
- Login en `/{tenant-domain}/`
- Tabla `users` propia de cada DB tenant
- Roles y permisos via Spatie (tabla `roles`, `permissions`, `model_has_roles`)
- Los permisos disponibles están asociados al paquete contratado (Package → `permission_id`)

### Localización y Tema
- Cookie `language` → `App::setLocale()` (seteado en `SuperAdminAuth` y `Common`)
- Cookie `theme` → compartido con vistas como `light` o `dark`

---

## Servicios Principales

### `DinamicConnection` (`app/Services/DinamicConnection.php`)
Cambia en tiempo de ejecución la base de datos del template `saleprosaas_tenant`:
```php
$this->setConnection($databaseName); // purge + reconnect
```
Usado para operaciones cross-tenant desde el contexto landlord.

### `FacturacionElectronicaService` (`app/Services/FacturacionElectronicaService.php`)
Cliente SOAP para los servicios web del SRI Ecuador:
- `enviarComprobanteSri($xml, $ambiente)` → valida el XML vía SOAP
- `autorizarComprobanteSri($claveAcceso, $ambiente)` → autoriza el comprobante
- Environments: `1` = pruebas (`celcer.sri.gob.ec`), `2` = producción (`cel.sri.gob.ec`)

### `ProcesarDocumentoService` (`app/Services/ProcesarDocumentoService.php`)
Orquesta el ciclo completo de un comprobante electrónico:
1. Validar emisor (`validarEmisor()`)
2. Firmar el XML con el certificado P12 de la empresa
3. Enviar al SRI para validación
4. Actualizar el campo `mensaje_sri` del documento

### `BackgroundAuthService` (`app/Services/BackgroundAuthService.php`)
Autenticación para tareas programadas y jobs en background.

### `PaymentService` (`app/Services/PaymentService.php`)
Abstracción unificada para pagos SaaS (Stripe, PayPal, Razorpay, Paystack).

---

## Librerías Internas

### `FacturacionElectronica` (`app/Librerias/FacturacionElectronica.php`)
Librería core para documentos electrónicos:
- `leerDocumentoXml($xml)` → normaliza y sanitiza XML (elimina tildes, caracteres especiales)
- `firmarXml($claveFirma, $firma, $nombreArchivo, $ruc)` → firma con XMLSecLibs usando certificado P12
- `leerCertificado($ruta)` → lee el archivo `.p12`
- Valida vigencia del certificado digital

### `Validadores` (`app/Librerias/Validadores.php`)
Validaciones específicas de Ecuador (RUC, CI, etc.).

---

## Constructores de Documentos XML (`app/FacturacionElectronica/`)

```
FacturacionElectronica/
├── Comprobante.php           → Clase base / orquestador
├── InfoTributaria.php        → Datos tributarios del emisor
├── Impuesto.php              → Impuestos (IVA, ICE)
├── TotalImpuesto.php
├── Pagos.php                 → Formas de pago
├── infoAdicional.php
├── Factura/                  → Tipo 01: Factura de venta
├── liquidacion/              → Tipo 03: Liquidación de compra
├── nota_credito/             → Tipo 04: Nota de crédito
│   ├── NotaCredito.php
│   └── InfoNotaCredito.php
├── nota_debito/              → Tipo 05: Nota de débito
│   ├── NotaDebito.php
│   └── InfoNotaDebito.php
└── (Guía de remisión Tipo 06, Retención Tipo 07 en controladores)
```

---

## PleskApiClient (`app/PleskApiClient.php`)

Integra con el servidor Plesk para aprovisionar automáticamente dominios y bases de datos cuando se crea un nuevo tenant.

---

## Migraciones

```
database/migrations/
├── landlord/   → Solo se aplican a la DB central
└── tenant/     → Se aplican a cada DB de tenant
```

- Para correr solo landlord: `php artisan migrate --path=database/migrations/landlord`
- Para correr en todos los tenants: `php artisan tenants:migrate`

---

## Caché

- El middleware `Common` usa `Cache::remember('general_setting', ...)` con TTL de 1 año
- El landlord usa `cache()->has('general_setting')` con fallback a DB
- Limpiar todo: `php artisan optimize:clear` o `GET /clear` (disponible en ambos contextos)

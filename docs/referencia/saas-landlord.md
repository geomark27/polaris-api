# SaaS — Gestión de Tenants (Landlord)

## Estructura Landlord

Todo el código landlord está en:
- Controladores: `app/Http/Controllers/landlord/`
- Modelos: `app/Models/landlord/`
- Vistas: `resources/views/landlord/`
- Rutas: `routes/web.php`

---

## Ciclo de Vida de un Tenant

### 1. Registro / Creación

**Ruta:** `POST /new_client` → `ClientController::store()`

```
Registro nuevo tenant
       │
       ├─ Valida RUC (Validadores::isValidRUC)
       ├─ Verifica que el RUC no tenga dominio existente
       ├─ Selecciona Package y determina expiración:
       │     free trial → general_settings::free_trial_limit días
       │     monthly    → 30 días
       │     yearly     → 365 días
       ├─ Crea Tenant (Stancl) → genera DB propia del tenant
       ├─ Crea Domain vinculado al RUC
       ├─ Agrega subdominio en Plesk (PleskApiClient) si corresponde
       ├─ Corre migraciones del tenant
       ├─ Seed inicial: usuario admin, configuración básica, bodegas
       ├─ Crea TenantPayment con método y monto
       └─ Envía email de bienvenida (Mail::to()->send(TenantCreate))
```

El campo `tenant_id` de Stancl es el **RUC** de la empresa, que también es el subdominio del tenant.

### 2. Renovación de Suscripción

**Métodos:** `ClientController::renew()`, `ClientController::selfRenew()`, `ClientController::renewSubscription()`

- Extiende `expiry_date` en `general_settings` del tenant
- Registra el pago en `TenantPayment`
- Puede ser iniciado por el super admin o por el propio tenant

### 3. Cambio de Paquete

**Método:** `ClientController::changePackage()`

- Actualiza `package_id` en `general_settings` del tenant
- Actualiza los permisos disponibles según el nuevo paquete

### 4. Verificación de Expiración

En cada request del tenant, el middleware `Common` verifica:
```php
if ($currentDateTimeFormatted > $expiryDateFormatted) {
    auth()->logout();
    return redirect('https://' . env('CENTRAL_DOMAIN') . '/contact-for-renewal?id=' . $subdomain);
}
```

### 5. Baja / Reset

**Método:** `ClientController::destroy()` — elimina el tenant y su base de datos
**Método:** `ClientController::resetDB()` — reinicia la base de datos del tenant manteniendo la estructura

---

## Paquetes (Packages)

**Modelo:** `app/Models/landlord/Package.php`
**Controlador:** `landlord/PackageController`

Cada paquete define:
- `features` (JSON): lista de funcionalidades habilitadas (ej: `woocommerce`, `ecommerce`)
- `permission_id` (CSV): IDs de permisos Spatie que se asignan al tenant
- `is_free_trial`: si aplica período de prueba gratuito
- `is_active`: si está disponible para contratar
- `stripe_price_id` (JSON): precios en Stripe para pago recurrente monthly/yearly

Al crear un tenant, los permisos del paquete se cargan en la DB del tenant mediante `role_permission_values`.

### Feature Gating

`PlansVerifyController::getPackageDetails()` consulta la tabla `packages` de la DB landlord desde el contexto del tenant (cross-connection) para verificar límites y características disponibles.

---

## Pagos SaaS

### Pasarelas de Pago Soportadas

| Pasarela | Implementación |
|---|---|
| Stripe | `StripeWebhookController`, suscripciones recurrentes |
| PayPal | `Payment/PaypalController`, `PaypalTenantController` |
| Razorpay | `Payment/PaymentController` |
| Paystack | `Payment/PaymentController::handleGatewayCallback()` |

### Flujo de Pago (nuevo plan)

```
Landing page → selecciona plan
       │
       ▼
POST /tenant-checkout → PaymentController::tenantCheckout()
       │
       ▼ (según pasarela)
Stripe  → genera Checkout Session → webhook confirma → actualiza tenant
PayPal  → redirect a PayPal → callback → actualiza tenant
Otros   → redirect → callback → actualiza tenant
```

### Webhook Stripe

`StripeWebhookController::handleWebhook()`:
- Recibe eventos de Stripe (pago exitoso, factura pagada, suscripción cancelada)
- Actualiza `expiry_date` del tenant en la DB landlord
- Genera factura para el tenant (`viewInvoice`)

---

## Landing Page (CMS)

El landlord incluye un CMS para la página de presentación pública:

| Sección | Controlador | Modelo |
|---|---|---|
| Heroes (banners) | `HeroController` | `Hero` |
| Features (características) | `FeaturesController` | `Features` |
| Módulos | `ModuleController` | `Module`, `ModuleDescription` |
| FAQs | `FaqController` | `Faq`, `FaqDescription` |
| Testimoniales | `TestimonialController` | `Testimonial` |
| Blogs | `BlogController` | `Blog` |
| Páginas CMS | `PageController` | `Page` |
| Redes Sociales | `SocialController` | `Social` |

El contenido multilenguaje usa `lang_id` para vincular descripciones en diferentes idiomas.

**Caché de landing:** heroes, módulos, FAQs y descripciones de signup se cachean con claves específicas y se invalidan en `PackageController::store()` vía el trait `CacheForget`.

---

## Multi-Acceso y Soporte

**Método:** `Auth\SuperAdminLoginController::multiAccess()`
- Permite al super admin ingresar a la sesión de un tenant específico sin contraseña
- Útil para soporte técnico

**Módulo de Tickets:** `TicketTenantController`
**Modelo:** `landlord/Tickets`
- Sistema de soporte interno con tickets por tenant

---

## Instalación del Sistema (SaaS Install)

**Ruta:** `GET /saas/install/step-{1,2,3,4}` → `SaasInstallController`

Wizard de instalación inicial del sistema:
1. Step 1: Verificación de requisitos
2. Step 2: Configuración de DB landlord
3. Step 3: Creación de super admin
4. Step 4: Finalización y seed inicial

---

## Modelo `GeneralSetting` del Tenant

Cada tenant tiene su propio `general_settings` que incluye:

| Campo | Descripción |
|---|---|
| `package_id` | Paquete contratado actualmente |
| `expiry_date` | Fecha de vencimiento de la suscripción |
| `free_trial_limit` | Días del período de prueba gratuito |
| `currency_id`, `currency_symbol` | Moneda por defecto |
| `date_format` | Formato de fechas |
| `stripe_key`, `stripe_secret` | Credenciales Stripe del tenant |
| `paypal_*`, `razorpay_*`, `paystack_*` | Credenciales otras pasarelas |
| `frontend_layout` | Diseño de la landing |

---

## Flujo de Auto-Renovación (Self-Service)

El tenant puede renovar su propia suscripción desde dentro de la app:

```
GET /self-subscription → ClientController::selfSuscription()
GET /self-package → ClientController::selfPackage()
GET /self-permission → ClientController::selfPermissions()
GET /renew-with-paypal/{pack_id}/{sub_type} → PaymentController::planPaypalRenew()
```

Estos endpoints son accesibles desde el dominio del tenant pero ejecutan lógica en la DB landlord.

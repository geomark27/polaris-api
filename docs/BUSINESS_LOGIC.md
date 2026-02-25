# Business Logic — Polaris ERP

_Última actualización: 2026-02-25 (Sprint 5 en progreso — módulos SystemValue y Product completados)_

---

## Propósito del sistema

Polaris ERP es una plataforma de gestión empresarial multi-módulo orientada a tenants. La capa que aquí se documenta es la API REST del tenant, responsable de manejar las operaciones de negocio de cada organización que use el sistema.

El sistema está diseñado para crecer de forma modular: cada dominio de negocio (usuarios, inventario, facturación, etc.) vive en su propio módulo independiente.

---

## Entidades del dominio

### User (Usuario)

Representa a cualquier persona que interactúa con el sistema dentro de un tenant. Puede ser un administrador, un empleado, un operador, etc.

Un usuario tiene identidad única garantizada por dos campos: `username` y `email`. Ninguno de los dos puede repetirse en el sistema.

El ciclo de vida de un usuario es:

```
Creación → Activo → (Desactivado) → Eliminado lógicamente
```

La eliminación nunca borra el registro de la base de datos. Se marca `deleted_at` con la fecha de eliminación y `is_active` en `false`, preservando el historial y la integridad referencial con otros módulos que en el futuro hagan referencia al usuario.

---

### SystemValue (Valor de catálogo)

Representa un valor parametrizable del sistema, agrupado bajo un tipo de catálogo (`catalog_type`). Es el mecanismo central para definir listas de opciones configurables que otras entidades utilizan como referencia sin necesidad de enums fijos en código.

La combinación `(catalogType, value)` es única en el sistema. El campo `value` se almacena en mayúsculas para garantizar consistencia.

**Catálogos iniciales sembrados al arrancar:**

| catalogType | Valores disponibles |
|---|---|
| `PRODUCT_TYPE` | `STANDARD`, `SERVICE`, `DIGITAL`, `RAW_MATERIAL` |
| `PRODUCT_TRACKING` | `NONE`, `LOT`, `SERIAL` |
| `UOM` | `PCS`, `KG`, `LT`, `MT`, `BOX`, `HOUR` |
| `CURRENCY` | `USD`, `PEN`, `EUR` |
| `DOCUMENT_STATUS` | `DRAFT`, `CONFIRMED`, `DONE`, `CANCELLED` |

Estos valores pueden extenderse vía API en cualquier momento sin necesidad de despliegue.

---

### ProductCategory (Categoría de producto)

Representa una clasificación jerárquica de productos. Las categorías se organizan en un árbol de profundidad ilimitada: cada categoría puede tener una categoría padre y múltiples hijos.

El campo `level` (nivel en el árbol) se calcula automáticamente al persistir: `level = parent.level + 1` si tiene padre, o `0` si es raíz.

**Regla de integridad:** no se puede eliminar una categoría que tenga hijos activos. Se debe eliminar o reasignar los hijos primero.

---

### Product (Producto)

Representa un artículo o servicio que la empresa puede comprar, vender o fabricar. Es la entidad central del módulo de inventario y ventas.

Un producto tiene identidad única por `code` y opcionalmente por `barcode`. Ambos campos se almacenan en mayúsculas.

El producto referencia valores de catálogo (`SystemValue`) para sus atributos enumerados:
- `productType` → catálogo `PRODUCT_TYPE`
- `tracking` → catálogo `PRODUCT_TRACKING`
- `unitOfMeasure` y `purchaseUom` → catálogo `UOM`
- `currency` → catálogo `CURRENCY`

La separación entre `unitOfMeasure` (UOM de venta) y `purchaseUom` (UOM de compra) permite manejar conversiones de unidades, por ejemplo: comprar por caja (`BOX`) y vender por pieza (`PCS`).

---

## Flujos de negocio

### Registro de usuario

```
Cliente envía: { username, email, password, phoneNumber? }
       ↓
Validación de formato (username 3-50 chars, email válido, password ≥ 8 chars)
       ↓
Verificación de unicidad: ¿ya existe ese username? ¿ya existe ese email?
       ↓
  Sí → Error (conflicto)
       ↓
  No → Se hashea la contraseña con BCrypt
       ↓
Se persiste el usuario con is_active = true, created_at = now()
       ↓
Se retorna el usuario sin exponer el hash de contraseña
```

### Actualización de usuario

Solo se pueden modificar `username` y `phone_number`. El `email` y la `password` son inmutables a través de este flujo (requieren flujos dedicados, ej: cambio de contraseña con verificación, cambio de email con confirmación).

Si se cambia el `username`, se verifica nuevamente que no esté tomado.

### Eliminación lógica (soft delete)

```
Cliente solicita DELETE /api/v1/users/{id}   (o cualquier entidad con soft-delete)
       ↓
Se verifica que el registro exista y no esté ya eliminado
       ↓
Se registra deleted_at = now() y is_active = false
       ↓
El registro deja de aparecer en listados y búsquedas normales
       ↓
El registro permanece en BD para auditoría e integridad referencial
```

Este patrón aplica de forma idéntica a `SystemValue`, `ProductCategory` y `Product`.

---

### Autenticación

El sistema usa autenticación stateless basada en JWT. No existe sesión en el servidor.

```
Cliente envía: { username, password }
       ↓
Se busca el usuario activo por username
       ↓
Se verifica la contraseña contra el hash BCrypt almacenado
       ↓
  Falla → Error 401 (credenciales inválidas — mismo mensaje para usuario inexistente y password incorrecta, por seguridad)
       ↓
  OK → Se generan dos tokens:
         accessToken  (válido 24h)  → para autenticar cada request
         refreshToken (válido 7d)   → para obtener un nuevo accessToken sin re-login
       ↓
Cliente incluye el accessToken en cada request:
  Authorization: Bearer <accessToken>
       ↓
Servidor valida firma y expiración → autoriza o rechaza
```

Cuando el `accessToken` expira, el cliente usa el `refreshToken` para obtener un par de tokens nuevo sin que el usuario tenga que ingresar sus credenciales otra vez.

### Logout

```
Cliente envía: POST /api/v1/auth/logout  (con Authorization: Bearer <accessToken>)
       ↓
Se extrae el jti (ID único del token)
       ↓
Se guarda el jti en la tabla revoked_tokens con su fecha de expiración
       ↓
Desde ese momento, cualquier request con ese token es rechazado
aunque la firma sea válida y no haya expirado aún
       ↓
Un job automático limpia la tabla cada medianoche
eliminando tokens cuya expiración ya pasó
```

---

### Creación de producto

```
Cliente envía: { code, name, productType, ... }
       ↓
Validación de formato (code y name requeridos, productType requerido)
       ↓
Verificación de unicidad: ¿ya existe ese code? ¿ya existe ese barcode?
       ↓
  Sí → Error (conflicto)
       ↓
  No → Si se proporcionó categoryId, se verifica que la categoría exista y no esté eliminada
       ↓
Se persiste con valores por defecto si no se especifican:
  tracking = NONE, unitOfMeasure = PCS, currency = USD
  salePrice = 0, costPrice = 0, minStock = 0, maxStock = 0
       ↓
Se retorna el producto con categoryId y categoryName resueltos del join
```

### Gestión del árbol de categorías

```
Crear categoría raíz:
  { name, displayOrder }
  → level = 0, parent = null
  → Se valida unicidad del nombre entre categorías raíz

Crear subcategoría:
  { name, parentId, displayOrder }
  → Se verifica que el parent exista y no esté eliminado
  → level = parent.level + 1
  → Se valida unicidad del nombre entre hijos del mismo padre

Eliminar categoría:
  → Se verifica que no tenga hijos activos
  → Si tiene hijos: Error — debe eliminar o reasignar hijos primero
  → Si no tiene hijos: soft delete (deleted_at + is_active = false)
```

---

## Reglas de negocio

### Usuarios
- `username` y `email` son únicos en todo el sistema (constraints a nivel de BD).
- La contraseña nunca se almacena en texto plano; siempre se guarda su hash BCrypt.
- La contraseña nunca se expone en ninguna respuesta de la API.
- Un usuario eliminado lógicamente no puede ser encontrado por las operaciones normales (findById, findAll).
- `email` no es actualizable mediante el endpoint de actualización general — requiere un flujo propio con verificación. <!-- TODO: implementar flujo de cambio de email -->
- `password` no es actualizable mediante el endpoint de actualización general — requiere un flujo propio con validación de contraseña actual. <!-- TODO: implementar flujo de cambio de contraseña -->
- Los campos `created_at` y `updated_at` son gestionados automáticamente por la capa de persistencia, no por el cliente.
- `phone_number` es opcional en la creación.
- Las credenciales inválidas siempre devuelven el mismo mensaje de error (no se distingue si el usuario no existe o si la contraseña es incorrecta) para no dar pistas a atacantes.
- Una cuenta desactivada (`is_active = false`) no puede autenticarse aunque las credenciales sean correctas.
- Solo los `accessToken` son válidos para autenticar requests. Los `refreshToken` son rechazados en los endpoints protegidos.
- Las rutas públicas (sin JWT) son exclusivamente `/api/v1/auth/login` y `/api/v1/auth/refresh`. El logout sí requiere JWT válido.
- Un token revocado (logout) es rechazado inmediatamente aunque no haya expirado — el `jti` único por token garantiza esto.

### SystemValue
- La combinación `(catalogType, value)` es única en el sistema.
- `catalogType` y `value` se normalizan a mayúsculas al persistir.
- Solo `label`, `description` y `displayOrder` son editables. `catalogType` y `value` son inmutables tras la creación.
- Los system values eliminados lógicamente no aparecen en los listados ni en el endpoint de catálogo (`/catalog/{type}`).

### ProductCategory
- El nombre de una categoría debe ser único dentro del mismo nivel (mismo padre). Dos categorías con distinto padre pueden tener el mismo nombre.
- El `level` es calculado automáticamente y no es editable directamente.
- El padre de una categoría no se puede cambiar tras la creación (solo se editan `name`, `description` y `displayOrder`).
- No se puede eliminar una categoría que tenga hijos activos.

### Product
- `code` y `barcode` son únicos en todo el sistema.
- `code` y `barcode` se normalizan a mayúsculas al persistir.
- `productType`, `tracking`, `unitOfMeasure`, `purchaseUom` y `currency` deben corresponder a valores existentes en los catálogos de `SystemValue` — la validación es semántica (el cliente envía el `value` correcto del catálogo). <!-- TODO: considerar validación automática contra SystemValue -->
- Los precios (`salePrice`, `costPrice`) y stocks (`minStock`, `maxStock`) no pueden ser negativos.
- La categoría referenciada debe existir y no estar eliminada.

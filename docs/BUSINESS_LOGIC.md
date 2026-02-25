# Business Logic — Polaris ERP

_Última actualización: 2026-02-22 (Sprint 3 completado — logout implementado)_

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
Cliente solicita DELETE /api/v1/users/{id}
       ↓
Se verifica que el usuario exista y no esté ya eliminado
       ↓
Se registra deleted_at = now() y is_active = false
       ↓
El usuario deja de aparecer en listados y búsquedas normales
       ↓
El registro permanece en BD para auditoría e integridad referencial
```

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

## Reglas de negocio

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

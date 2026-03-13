# Sprint 2 - Guia de Implementacion: Cambio de Contrasena

Objetivo: cerrar la tarea pendiente de Sprint 2 para cambio de contrasena con validacion de contrasena actual.

## Estado: COMPLETADO

---

## 1. Alcance del flujo

Flujo de autoservicio para usuario autenticado.

- Endpoint: `PATCH /api/v1/users/me/password`
- Requiere JWT valido
- El usuario solo puede cambiar su propia contrasena

---

## 2. Lo que se implemento

### 2.1 DTO — `ChangePasswordRequest`

Ubicacion: `src/main/java/com/azenticsys/polaris/user/dto/ChangePasswordRequest.java`

```java
@ValidChangePassword
public record ChangePasswordRequest(
    @NotBlank String currentPassword,
    @NotBlank @Size(min = 8) String newPassword,
    @NotBlank String confirmNewPassword
) {}
```

Anotacion personalizada `@ValidChangePassword` delega al validador `ChangePasswordRequestValidator`.

### 2.2 Validador personalizado — `ChangePasswordRequestValidator`

Ubicacion: `src/main/java/com/azenticsys/polaris/user/validation/`

Valida dos reglas de negocio a nivel de clase:
1. `newPassword` != `currentPassword` → 400 con mensaje en campo `newPassword`
2. `newPassword` == `confirmNewPassword` (deben coincidir) → 400 con mensaje en campo `confirmNewPassword`

### 2.3 Interfaz — `UserService`

```java
void changePassword(String username, ChangePasswordRequest request);
```

El `username` se obtiene del JWT autenticado, no del body.

### 2.4 Implementacion — `UserServiceImpl`

```java
@Override
@Transactional
public void changePassword(String username, ChangePasswordRequest request) {
    User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
        .filter(User::isActive)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
        throw new BadCredentialsException("Current password is incorrect");
    }

    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
}
```

Notas:
- Filtra `deletedAt == null` Y `isActive == true` antes de proceder
- `BadCredentialsException` → el `GlobalExceptionHandler` la mapea a `401`
- `@Transactional` para garantizar consistencia

### 2.5 Endpoint — `UserController`

```java
@PatchMapping("/me/password")
public ResponseEntity<Void> changePassword(
    @Valid @RequestBody ChangePasswordRequest request,
    Authentication authentication
) {
    userService.changePassword(authentication.getName(), request);
    return ResponseEntity.noContent().build();
}
```

---

## 3. Manejo de errores

| Caso | Excepcion | HTTP |
|---|---|---|
| Campos vacios o password < 8 chars | `MethodArgumentNotValidException` | 400 |
| `newPassword == currentPassword` | `MethodArgumentNotValidException` | 400 |
| `newPassword != confirmNewPassword` | `MethodArgumentNotValidException` | 400 |
| Contrasena actual incorrecta | `BadCredentialsException` | 401 |
| Usuario no encontrado o inactivo | `IllegalArgumentException` | 400 |

---

## 4. Pruebas manuales (Postman/Insomnia)

1. Login → obtener access token
2. `PATCH /api/v1/users/me/password` con password actual correcta → `204`
3. Login con password anterior → debe fallar (`401`)
4. Login con password nueva → debe funcionar
5. Reintento con `currentPassword` incorrecta → `401`
6. `newPassword` corta (< 8) → `400`
7. `newPassword == currentPassword` → `400`
8. `newPassword != confirmNewPassword` → `400`

---

## 5. Errores corregidos durante implementacion

- El validador tenia la logica de `confirmNewPassword` **invertida**: verificaba que SÍ coincidieran y seteaba `valid = true`, cuando debía verificar que NO coincidieran y setear `valid = false`.
- El endpoint usaba `@PostMapping("/change-password")` en lugar de `@PatchMapping("/me/password")` (violaba convencion REST: sustantivos en URL, verbos en metodo HTTP).
- El response era `200 OK` en lugar de `204 No Content`.
- La excepcion de password incorrecta usaba `ResponseStatusException(BAD_REQUEST)` en lugar de `BadCredentialsException` (estatus incorrecto: era `400` en vez de `401`).
- Faltaban `@Override` y `@Transactional` en el metodo de implementacion.
- No se verificaba `isActive` en el usuario — solo filtraba `deletedAt`.

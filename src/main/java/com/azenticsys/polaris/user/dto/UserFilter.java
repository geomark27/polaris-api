package com.azenticsys.polaris.user.dto;

/**
 * Filtros opcionales para la búsqueda de usuarios.
 * Todos los campos son opcionales; null = sin filtro.
 * Las búsquedas de texto usan LIKE %valor% (case-insensitive).
 */
public record UserFilter(
        String username,
        String email,
        String phoneNumber
) {
    public boolean isEmpty() {
        return (username == null || username.isBlank())
                && (email == null || email.isBlank())
                && (phoneNumber == null || phoneNumber.isBlank());
    }
}

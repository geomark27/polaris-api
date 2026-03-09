package com.azenticsys.polaris.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        /**
         * Slug único de la organización. Solo minúsculas, números y guiones.
         * Ej: "torres-y-torres", "acme", "mi-empresa-2024"
         */
        @NotBlank(message = "Slug is required")
        @Size(min = 3, max = 63, message = "Slug must be between 3 and 63 characters")
        @Pattern(
                regexp = "^[a-z0-9][a-z0-9\\-]*[a-z0-9]$",
                message = "Slug must contain only lowercase letters, numbers and hyphens, and cannot start or end with a hyphen"
        )
        String slug,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}

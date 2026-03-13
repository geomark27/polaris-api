package com.azenticsys.polaris.company.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCompanyRequest(

        // TODO: agregar campos con sus validaciones
        @NotBlank(message = "Field is required")
        String name
) {}

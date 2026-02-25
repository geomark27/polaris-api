package com.azenticsys.polaris.systemvalue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSystemValueRequest(

        @NotBlank(message = "Catalog type is required")
        @Size(max = 50, message = "Catalog type must not exceed 50 characters")
        String catalogType,

        @NotBlank(message = "Value is required")
        @Size(max = 100, message = "Value must not exceed 100 characters")
        String value,

        @NotBlank(message = "Label is required")
        @Size(max = 200, message = "Label must not exceed 200 characters")
        String label,

        String description,

        int displayOrder
) {}

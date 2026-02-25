package com.azenticsys.polaris.systemvalue.dto;

import jakarta.validation.constraints.Size;

public record UpdateSystemValueRequest(

        @Size(max = 200, message = "Label must not exceed 200 characters")
        String label,

        String description,

        Integer displayOrder
) {}

package com.azenticsys.polaris.product.dto;

import jakarta.validation.constraints.Size;

public record UpdateProductCategoryRequest(

        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        String description,

        Integer displayOrder
) {}

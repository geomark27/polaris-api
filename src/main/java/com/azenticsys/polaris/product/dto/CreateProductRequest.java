package com.azenticsys.polaris.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(

        @NotBlank(message = "Code is required")
        @Size(max = 50, message = "Code must not exceed 50 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        String description,

        @Size(max = 100, message = "Barcode must not exceed 100 characters")
        String barcode,

        @NotBlank(message = "Product type is required")
        String productType,

        UUID categoryId,

        boolean canBeSold,

        boolean canBePurchased,

        boolean canBeManufactured,

        String tracking,

        String unitOfMeasure,

        String purchaseUom,

        @DecimalMin(value = "0.0", message = "Sale price must be >= 0")
        BigDecimal salePrice,

        @DecimalMin(value = "0.0", message = "Cost price must be >= 0")
        BigDecimal costPrice,

        String currency,

        @DecimalMin(value = "0.0", message = "Weight must be >= 0")
        BigDecimal weight,

        @DecimalMin(value = "0.0", message = "Volume must be >= 0")
        BigDecimal volume,

        @DecimalMin(value = "0.0", message = "Min stock must be >= 0")
        BigDecimal minStock,

        @DecimalMin(value = "0.0", message = "Max stock must be >= 0")
        BigDecimal maxStock
) {}

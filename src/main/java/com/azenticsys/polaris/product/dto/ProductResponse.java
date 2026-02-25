package com.azenticsys.polaris.product.dto;

import com.azenticsys.polaris.product.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String code,
        String name,
        String description,
        String barcode,
        String productType,
        UUID categoryId,
        String categoryName,
        boolean canBeSold,
        boolean canBePurchased,
        boolean canBeManufactured,
        String tracking,
        String unitOfMeasure,
        String purchaseUom,
        BigDecimal salePrice,
        BigDecimal costPrice,
        String currency,
        BigDecimal weight,
        BigDecimal volume,
        BigDecimal minStock,
        BigDecimal maxStock,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product entity) {
        UUID categoryId   = entity.getCategory() != null ? entity.getCategory().getId()   : null;
        String categoryName = entity.getCategory() != null ? entity.getCategory().getName() : null;

        return new ProductResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getBarcode(),
                entity.getProductType(),
                categoryId,
                categoryName,
                entity.isCanBeSold(),
                entity.isCanBePurchased(),
                entity.isCanBeManufactured(),
                entity.getTracking(),
                entity.getUnitOfMeasure(),
                entity.getPurchaseUom(),
                entity.getSalePrice(),
                entity.getCostPrice(),
                entity.getCurrency(),
                entity.getWeight(),
                entity.getVolume(),
                entity.getMinStock(),
                entity.getMaxStock(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

package com.azenticsys.polaris.product.dto;

import com.azenticsys.polaris.product.entity.ProductCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductCategoryResponse(
        UUID id,
        String name,
        String description,
        UUID parentId,
        String parentName,
        int level,
        int displayOrder,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductCategoryResponse from(ProductCategory entity) {
        UUID parentId   = entity.getParent() != null ? entity.getParent().getId()   : null;
        String parentName = entity.getParent() != null ? entity.getParent().getName() : null;

        return new ProductCategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                parentId,
                parentName,
                entity.getLevel(),
                entity.getDisplayOrder(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

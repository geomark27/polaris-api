package com.azenticsys.polaris.systemvalue.dto;

import com.azenticsys.polaris.systemvalue.entity.SystemValue;

import java.time.LocalDateTime;
import java.util.UUID;

public record SystemValueResponse(
        UUID id,
        String catalogType,
        String value,
        String label,
        String description,
        int displayOrder,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SystemValueResponse from(SystemValue entity) {
        return new SystemValueResponse(
                entity.getId(),
                entity.getCatalogType(),
                entity.getValue(),
                entity.getLabel(),
                entity.getDescription(),
                entity.getDisplayOrder(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

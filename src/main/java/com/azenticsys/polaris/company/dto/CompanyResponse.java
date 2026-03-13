package com.azenticsys.polaris.company.dto;

import com.azenticsys.polaris.company.entity.Company;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        // TODO: agregar campos de la respuesta
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CompanyResponse from(Company entity) {
        return new CompanyResponse(
                entity.getId(),
                // TODO: mapear campos
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

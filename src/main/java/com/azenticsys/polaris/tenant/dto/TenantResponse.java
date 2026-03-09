package com.azenticsys.polaris.tenant.dto;

import com.azenticsys.polaris.tenant.entity.Tenant;

import java.time.LocalDateTime;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String schemaName,
        String email,
        boolean isActive,
        LocalDateTime createdAt
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getSchemaName(),
                tenant.getEmail(),
                tenant.isActive(),
                tenant.getCreatedAt()
        );
    }
}

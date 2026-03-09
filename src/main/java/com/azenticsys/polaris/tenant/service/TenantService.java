package com.azenticsys.polaris.tenant.service;

import com.azenticsys.polaris.tenant.dto.CreateTenantRequest;
import com.azenticsys.polaris.tenant.dto.TenantResponse;
import com.azenticsys.polaris.tenant.entity.Tenant;

import java.util.List;
import java.util.UUID;

public interface TenantService {

    /** Registra un nuevo tenant, crea su schema PostgreSQL y clona las tablas. */
    TenantResponse register(CreateTenantRequest request);

    /** Versión interna usada por DataInitializer: devuelve la entidad completa. */
    Tenant registerInternal(CreateTenantRequest request);

    TenantResponse findById(UUID id);

    List<TenantResponse> findAll();

    void deactivate(UUID id);
}

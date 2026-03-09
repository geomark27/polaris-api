package com.azenticsys.polaris.tenant.controller;

import com.azenticsys.polaris.tenant.dto.CreateTenantRequest;
import com.azenticsys.polaris.tenant.dto.TenantResponse;
import com.azenticsys.polaris.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * Registra una nueva organización (tenant) en el sistema.
     * Crea su schema PostgreSQL y tablas automáticamente.
     * Endpoint público — usado en el flujo de onboarding.
     */
    @PostMapping("/register")
    public ResponseEntity<TenantResponse> register(@Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.register(request));
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> findAll() {
        return ResponseEntity.ok(tenantService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        tenantService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}

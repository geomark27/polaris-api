package com.azenticsys.polaris.tenant.service;

import com.azenticsys.polaris.tenant.dto.CreateTenantRequest;
import com.azenticsys.polaris.tenant.dto.TenantResponse;
import com.azenticsys.polaris.tenant.entity.Tenant;
import com.azenticsys.polaris.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    /**
     * Tablas del schema de cada tenant.
     * Se clonan desde el schema "public" (creado por Hibernate DDL al arrancar).
     * Actualizar esta lista cuando se agreguen nuevos módulos ERP.
     */
    private static final List<String> TENANT_TABLES = List.of(
            "users",
            "product_categories",
            "products",
            "system_values"
    );

    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public TenantResponse register(CreateTenantRequest request) {
        return TenantResponse.from(registerInternal(request));
    }

    @Override
    @Transactional
    public Tenant registerInternal(CreateTenantRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Slug '" + request.slug() + "' is already taken");
        }
        if (tenantRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email '" + request.email() + "' is already registered");
        }

        String schemaName = Tenant.toSchemaName(request.slug());

        Tenant tenant = Tenant.builder()
                .name(request.name())
                .slug(request.slug())
                .schemaName(schemaName)
                .email(request.email())
                .build();

        tenant = tenantRepository.save(tenant);

        createTenantSchema(schemaName);

        log.info("Tenant registered: slug='{}', schema='{}'", tenant.getSlug(), schemaName);
        return tenant;
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse findById(UUID id) {
        return TenantResponse.from(getOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> findAll() {
        return tenantRepository.findAll().stream()
                .map(TenantResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        Tenant tenant = getOrThrow(id);
        tenant.setActive(false);
        tenantRepository.save(tenant);
        log.info("Tenant deactivated: slug='{}'", tenant.getSlug());
    }

    // ─────────────────────────────────────────────────────────────────────
    // Schema management
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Crea el schema del tenant y clona todas las tablas desde el schema "public"
     * (template generado por Hibernate DDL al arrancar).
     *
     * LIKE ... INCLUDING ALL copia estructura, constraints NOT NULL, checks,
     * defaults e índices. Las foreign keys entre tablas del mismo tenant
     * se agregan explícitamente.
     */
    private void createTenantSchema(String schemaName) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);

        for (String table : TENANT_TABLES) {
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS " + schemaName + "." + table +
                    " (LIKE public." + table + " INCLUDING ALL)"
            );
        }

        // FK: products → product_categories (no se copia con LIKE)
        // PostgreSQL no soporta ADD CONSTRAINT IF NOT EXISTS, se usa DO block
        jdbcTemplate.execute(
                "DO $$ BEGIN " +
                "IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_products_category' " +
                "AND connamespace = '" + schemaName + "'::regnamespace) THEN " +
                "ALTER TABLE " + schemaName + ".products " +
                "ADD CONSTRAINT fk_products_category " +
                "FOREIGN KEY (category_id) REFERENCES " + schemaName + ".product_categories(id); " +
                "END IF; END $$"
        );

        log.info("Schema '{}' created with {} tables", schemaName, TENANT_TABLES.size());
    }

    private Tenant getOrThrow(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tenant not found: " + id));
    }
}

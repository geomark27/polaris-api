package com.azenticsys.polaris.tenant.repository;

import com.azenticsys.polaris.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findBySlugAndIsActiveTrue(String slug);

    boolean existsBySlug(String slug);

    boolean existsByEmail(String email);
}

package com.azenticsys.polaris.tenant.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de un tenant en el schema landlord.
 * El slug es el identificador público (ej: "torresytorres").
 * El schemaName es el schema PostgreSQL que le corresponde (ej: "t_torresytorres").
 */
@Entity
@Table(
        schema = "landlord",
        name = "tenants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tenants_slug",        columnNames = "slug"),
                @UniqueConstraint(name = "uq_tenants_schema_name", columnNames = "schema_name"),
                @UniqueConstraint(name = "uq_tenants_email",       columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Nombre descriptivo de la organización. Ej: "Torres y Torres S.A." */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Identificador URL-friendly único. Ej: "torresytorres".
     * Solo minúsculas, números y guiones. Equivalente al subdominio en apps como Atlassian.
     */
    @Column(name = "slug", nullable = false, length = 63)
    private String slug;

    /**
     * Nombre del schema PostgreSQL para este tenant. Ej: "t_torresytorres".
     * Derivado del slug: "t_" + slug.replace("-", "_").
     */
    @Column(name = "schema_name", nullable = false, length = 63, updatable = false)
    private String schemaName;

    /** Email de contacto principal del tenant. */
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /** Deriva el schema name a partir del slug. */
    public static String toSchemaName(String slug) {
        return "t_" + slug.toLowerCase().replace("-", "_");
    }
}

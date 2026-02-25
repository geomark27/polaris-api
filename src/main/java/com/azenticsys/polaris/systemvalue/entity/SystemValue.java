package com.azenticsys.polaris.systemvalue.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "system_values",
    // Índice compuesto equivalente al idx_system_values_catalog_value de Go
    // garantiza que no exista el mismo value para el mismo catalogType
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_system_values_catalog_value",
            columnNames = {"catalog_type", "value"}
        )
    },
    indexes = {
        @Index(
            name = "idx_system_values_catalog_type",
            columnList = "catalog_type"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemValue {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Agrupa valores bajo una misma categoría, ej: "PRODUCT_TYPE", "DOCUMENT_STATUS"
    @Column(name = "catalog_type", nullable = false, length = 50)
    private String catalogType;

    // Valor interno que usarán las demás entidades como FK lógica, ej: "STANDARD", "SERVICE"
    @Column(name = "value", nullable = false, length = 100)
    private String value;

    // Texto legible para mostrar en UI, ej: "Producto Estándar"
    @Column(name = "label", nullable = false, length = 200)
    private String label;

    // Descripción opcional para dar contexto del valor
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Controla el orden de aparición en selects/dropdowns
    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
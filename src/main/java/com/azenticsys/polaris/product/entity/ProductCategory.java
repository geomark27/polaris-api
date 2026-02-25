package com.azenticsys.polaris.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "product_categories",
    indexes = {
        @Index(name = "idx_product_categories_parent_id", columnList = "parent_id"),
        @Index(name = "idx_product_categories_is_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Relación auto-referencial: categoría padre (null = categoría raíz)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_product_categories_parent"))
    private ProductCategory parent;

    // Hijos directos de esta categoría
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductCategory> children = new ArrayList<>();

    // Nivel en el árbol: 0 = raíz, 1 = primer nivel, etc.
    // Se calcula al persistir, útil para queries y validaciones
    @Column(name = "level", nullable = false)
    @Builder.Default
    private int level = 0;

    // Orden de aparición entre categorías del mismo padre
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
        // Calcula el nivel automáticamente basado en el padre
        this.level = (parent != null) ? parent.getLevel() + 1 : 0;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
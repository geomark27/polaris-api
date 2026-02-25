package com.azenticsys.polaris.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "products",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_products_code",    columnNames = "code"),
        @UniqueConstraint(name = "uq_products_barcode", columnNames = "barcode")
    },
    indexes = {
        @Index(name = "idx_products_category_id",   columnList = "category_id"),
        @Index(name = "idx_products_product_type",  columnList = "product_type"),
        @Index(name = "idx_products_is_active",     columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ─────────────────────────────────────────────
    // Identificación y clasificación
    // ─────────────────────────────────────────────

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Código de barras EAN, UPC, etc.
    @Column(name = "barcode", length = 100)
    private String barcode;

    // Referencia al SystemValue de catalog_type = 'PRODUCT_TYPE'
    // Ej: STANDARD, SERVICE, DIGITAL, RAW_MATERIAL
    @Column(name = "product_type", nullable = false, length = 100)
    private String productType;

    // FK a ProductCategory (árbol jerárquico)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_products_category"))
    private ProductCategory category;

    // ─────────────────────────────────────────────
    // Comportamiento en el sistema
    // ─────────────────────────────────────────────

    @Column(name = "can_be_sold", nullable = false)
    @Builder.Default
    private boolean canBeSold = true;

    @Column(name = "can_be_purchased", nullable = false)
    @Builder.Default
    private boolean canBePurchased = true;

    @Column(name = "can_be_manufactured", nullable = false)
    @Builder.Default
    private boolean canBeManufactured = false;

    // Referencia al SystemValue de catalog_type = 'PRODUCT_TRACKING'
    // Ej: NONE, LOT, SERIAL
    @Column(name = "tracking", nullable = false, length = 100)
    @Builder.Default
    private String tracking = "NONE";

    // ─────────────────────────────────────────────
    // Unidades de medida
    // Referencia al SystemValue de catalog_type = 'UOM'
    // Ej: PCS, KG, LT, MT, BOX
    // ─────────────────────────────────────────────

    @Column(name = "unit_of_measure", nullable = false, length = 100)
    @Builder.Default
    private String unitOfMeasure = "PCS";

    // UOM con la que se compra, puede diferir de la de venta
    // Ej: compras por BOX, vendes por PCS
    @Column(name = "purchase_uom", length = 100)
    private String purchaseUom;

    // ─────────────────────────────────────────────
    // Precios
    // ─────────────────────────────────────────────

    @Column(name = "sale_price", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal salePrice = BigDecimal.ZERO;

    @Column(name = "cost_price", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    // Referencia al SystemValue de catalog_type = 'CURRENCY'
    // Ej: USD, PEN, EUR
    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "USD";

    // ─────────────────────────────────────────────
    // Logística / física
    // ─────────────────────────────────────────────

    // Peso en kg
    @Column(name = "weight", precision = 10, scale = 4)
    private BigDecimal weight;

    // Volumen en m³
    @Column(name = "volume", precision = 10, scale = 4)
    private BigDecimal volume;

    // Stock mínimo para alertas de reorden
    @Column(name = "min_stock", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal minStock = BigDecimal.ZERO;

    // Stock máximo permitido en almacén
    @Column(name = "max_stock", precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal maxStock = BigDecimal.ZERO;

    // ─────────────────────────────────────────────
    // Auditoría
    // ─────────────────────────────────────────────

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
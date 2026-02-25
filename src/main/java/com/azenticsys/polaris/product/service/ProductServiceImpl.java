package com.azenticsys.polaris.product.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.product.dto.CreateProductRequest;
import com.azenticsys.polaris.product.dto.ProductFilter;
import com.azenticsys.polaris.product.dto.ProductResponse;
import com.azenticsys.polaris.product.dto.UpdateProductRequest;
import com.azenticsys.polaris.product.entity.Product;
import com.azenticsys.polaris.product.entity.ProductCategory;
import com.azenticsys.polaris.product.repository.ProductCategoryRepository;
import com.azenticsys.polaris.product.repository.ProductRepository;
import com.azenticsys.polaris.product.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Product code already exists: " + request.code());
        }
        if (request.barcode() != null && !request.barcode().isBlank()
                && productRepository.existsByBarcode(request.barcode())) {
            throw new IllegalArgumentException("Barcode already registered: " + request.barcode());
        }

        ProductCategory category = resolveCategory(request.categoryId());

        Product product = Product.builder()
                .code(request.code().toUpperCase())
                .name(request.name())
                .description(request.description())
                .barcode(request.barcode())
                .productType(request.productType().toUpperCase())
                .category(category)
                .canBeSold(request.canBeSold())
                .canBePurchased(request.canBePurchased())
                .canBeManufactured(request.canBeManufactured())
                .tracking(request.tracking() != null ? request.tracking().toUpperCase() : "NONE")
                .unitOfMeasure(request.unitOfMeasure() != null ? request.unitOfMeasure().toUpperCase() : "PCS")
                .purchaseUom(request.purchaseUom() != null ? request.purchaseUom().toUpperCase() : null)
                .salePrice(request.salePrice() != null ? request.salePrice() : BigDecimal.ZERO)
                .costPrice(request.costPrice() != null ? request.costPrice() : BigDecimal.ZERO)
                .currency(request.currency() != null ? request.currency().toUpperCase() : "USD")
                .weight(request.weight())
                .volume(request.volume())
                .minStock(request.minStock() != null ? request.minStock() : BigDecimal.ZERO)
                .maxStock(request.maxStock() != null ? request.maxStock() : BigDecimal.ZERO)
                .build();

        return ProductResponse.from(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return ProductResponse.from(getActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAll(PageQuery pageQuery, ProductFilter filter) {
        return PageResponse.from(
                productRepository.findAll(
                        ProductSpecification.withFilter(filter),
                        pageQuery.toPageable()
                ).map(ProductResponse::from)
        );
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product entity = getActive(id);

        if (request.code() != null && !request.code().isBlank()
                && !request.code().equalsIgnoreCase(entity.getCode())) {
            if (productRepository.existsByCodeAndIdNot(request.code().toUpperCase(), id)) {
                throw new IllegalArgumentException("Product code already exists: " + request.code());
            }
            entity.setCode(request.code().toUpperCase());
        }
        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.barcode() != null) {
            if (!request.barcode().isBlank()
                    && productRepository.existsByBarcodeAndIdNot(request.barcode(), id)) {
                throw new IllegalArgumentException("Barcode already registered: " + request.barcode());
            }
            entity.setBarcode(request.barcode().isBlank() ? null : request.barcode());
        }
        if (request.productType() != null && !request.productType().isBlank()) {
            entity.setProductType(request.productType().toUpperCase());
        }
        if (request.categoryId() != null) {
            entity.setCategory(resolveCategory(request.categoryId()));
        }
        if (request.canBeSold() != null) {
            entity.setCanBeSold(request.canBeSold());
        }
        if (request.canBePurchased() != null) {
            entity.setCanBePurchased(request.canBePurchased());
        }
        if (request.canBeManufactured() != null) {
            entity.setCanBeManufactured(request.canBeManufactured());
        }
        if (request.tracking() != null && !request.tracking().isBlank()) {
            entity.setTracking(request.tracking().toUpperCase());
        }
        if (request.unitOfMeasure() != null && !request.unitOfMeasure().isBlank()) {
            entity.setUnitOfMeasure(request.unitOfMeasure().toUpperCase());
        }
        if (request.purchaseUom() != null) {
            entity.setPurchaseUom(request.purchaseUom().isBlank() ? null : request.purchaseUom().toUpperCase());
        }
        if (request.salePrice() != null) {
            entity.setSalePrice(request.salePrice());
        }
        if (request.costPrice() != null) {
            entity.setCostPrice(request.costPrice());
        }
        if (request.currency() != null && !request.currency().isBlank()) {
            entity.setCurrency(request.currency().toUpperCase());
        }
        if (request.weight() != null) {
            entity.setWeight(request.weight());
        }
        if (request.volume() != null) {
            entity.setVolume(request.volume());
        }
        if (request.minStock() != null) {
            entity.setMinStock(request.minStock());
        }
        if (request.maxStock() != null) {
            entity.setMaxStock(request.maxStock());
        }

        return ProductResponse.from(productRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Product entity = getActive(id);
        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
        productRepository.save(entity);
    }

    private Product getActive(UUID id) {
        return productRepository.findById(id)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    private ProductCategory resolveCategory(UUID categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found with id: " + categoryId
                ));
    }
}

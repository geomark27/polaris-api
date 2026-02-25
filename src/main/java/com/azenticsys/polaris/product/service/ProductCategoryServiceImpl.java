package com.azenticsys.polaris.product.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.product.dto.CreateProductCategoryRequest;
import com.azenticsys.polaris.product.dto.ProductCategoryFilter;
import com.azenticsys.polaris.product.dto.ProductCategoryResponse;
import com.azenticsys.polaris.product.dto.UpdateProductCategoryRequest;
import com.azenticsys.polaris.product.entity.ProductCategory;
import com.azenticsys.polaris.product.repository.ProductCategoryRepository;
import com.azenticsys.polaris.product.repository.ProductCategorySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductCategoryResponse create(CreateProductCategoryRequest request) {
        ProductCategory parent = null;

        if (request.parentId() != null) {
            parent = categoryRepository.findById(request.parentId())
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Parent category not found with id: " + request.parentId()
                    ));
        }

        validateUniqueName(request.name(), parent, null);

        ProductCategory category = ProductCategory.builder()
                .name(request.name())
                .description(request.description())
                .parent(parent)
                .displayOrder(request.displayOrder())
                .build();

        return ProductCategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductCategoryResponse findById(UUID id) {
        return ProductCategoryResponse.from(getActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductCategoryResponse> findAll(PageQuery pageQuery, ProductCategoryFilter filter) {
        return PageResponse.from(
                categoryRepository.findAll(
                        ProductCategorySpecification.withFilter(filter),
                        pageQuery.toPageable()
                ).map(ProductCategoryResponse::from)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> findRoots() {
        return categoryRepository.findByParentIsNullAndDeletedAtIsNullOrderByDisplayOrderAsc()
                .stream()
                .map(ProductCategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryResponse> findChildren(UUID parentId) {
        ProductCategory parent = getActive(parentId);
        return categoryRepository.findByParentAndDeletedAtIsNullOrderByDisplayOrderAsc(parent)
                .stream()
                .map(ProductCategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ProductCategoryResponse update(UUID id, UpdateProductCategoryRequest request) {
        ProductCategory entity = getActive(id);

        if (request.name() != null && !request.name().isBlank()) {
            validateUniqueName(request.name(), entity.getParent(), id);
            entity.setName(request.name());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.displayOrder() != null) {
            entity.setDisplayOrder(request.displayOrder());
        }

        return ProductCategoryResponse.from(categoryRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        ProductCategory entity = getActive(id);

        boolean hasActiveChildren = categoryRepository
                .findByParentAndDeletedAtIsNullOrderByDisplayOrderAsc(entity)
                .stream()
                .anyMatch(c -> c.getDeletedAt() == null);

        if (hasActiveChildren) {
            throw new IllegalArgumentException(
                    "Cannot delete category with active children. Remove or reassign children first."
            );
        }

        entity.setDeletedAt(LocalDateTime.now());
        entity.setActive(false);
        categoryRepository.save(entity);
    }

    private ProductCategory getActive(UUID id) {
        return categoryRepository.findById(id)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    private void validateUniqueName(String name, ProductCategory parent, UUID excludeId) {
        boolean exists;
        if (parent == null) {
            exists = excludeId == null
                    ? categoryRepository.existsByNameAndParentIsNull(name)
                    : categoryRepository.existsByNameAndParentIsNullAndIdNot(name, excludeId);
        } else {
            exists = excludeId == null
                    ? categoryRepository.existsByNameAndParent(name, parent)
                    : categoryRepository.existsByNameAndParentAndIdNot(name, parent, excludeId);
        }
        if (exists) {
            throw new IllegalArgumentException(
                    "Category name '" + name + "' already exists at this level"
            );
        }
    }
}

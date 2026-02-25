package com.azenticsys.polaris.product.controller;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.product.dto.CreateProductCategoryRequest;
import com.azenticsys.polaris.product.dto.ProductCategoryFilter;
import com.azenticsys.polaris.product.dto.ProductCategoryResponse;
import com.azenticsys.polaris.product.dto.UpdateProductCategoryRequest;
import com.azenticsys.polaris.product.service.ProductCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PostMapping
    public ResponseEntity<ProductCategoryResponse> create(
            @Valid @RequestBody CreateProductCategoryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategoryResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductCategoryResponse>> findAll(
            @RequestParam(defaultValue = "0")            int page,
            @RequestParam(defaultValue = "10")           int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc")          String sortDir,
            @RequestParam(required = false)              String name,
            @RequestParam(required = false)              UUID parentId,
            @RequestParam(required = false)              Integer level,
            @RequestParam(required = false)              Boolean isActive
    ) {
        PageQuery pageQuery = new PageQuery(page, size, sortBy, sortDir);
        ProductCategoryFilter filter = new ProductCategoryFilter(name, parentId, level, isActive);
        return ResponseEntity.ok(categoryService.findAll(pageQuery, filter));
    }

    @GetMapping("/roots")
    public ResponseEntity<List<ProductCategoryResponse>> findRoots() {
        return ResponseEntity.ok(categoryService.findRoots());
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<ProductCategoryResponse>> findChildren(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findChildren(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductCategoryResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductCategoryRequest request
    ) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}

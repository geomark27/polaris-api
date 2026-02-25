package com.azenticsys.polaris.product.controller;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.product.dto.CreateProductRequest;
import com.azenticsys.polaris.product.dto.ProductFilter;
import com.azenticsys.polaris.product.dto.ProductResponse;
import com.azenticsys.polaris.product.dto.UpdateProductRequest;
import com.azenticsys.polaris.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> findAll(
            @RequestParam(defaultValue = "0")          int page,
            @RequestParam(defaultValue = "10")         int size,
            @RequestParam(defaultValue = "createdAt")  String sortBy,
            @RequestParam(defaultValue = "desc")       String sortDir,
            @RequestParam(required = false)            String code,
            @RequestParam(required = false)            String name,
            @RequestParam(required = false)            String productType,
            @RequestParam(required = false)            UUID categoryId,
            @RequestParam(required = false)            Boolean isActive
    ) {
        PageQuery pageQuery = new PageQuery(page, size, sortBy, sortDir);
        ProductFilter filter = new ProductFilter(code, name, productType, categoryId, isActive);
        return ResponseEntity.ok(productService.findAll(pageQuery, filter));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}

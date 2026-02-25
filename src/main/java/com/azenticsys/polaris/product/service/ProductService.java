package com.azenticsys.polaris.product.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.product.dto.CreateProductRequest;
import com.azenticsys.polaris.product.dto.ProductFilter;
import com.azenticsys.polaris.product.dto.ProductResponse;
import com.azenticsys.polaris.product.dto.UpdateProductRequest;

import java.util.UUID;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse findById(UUID id);

    PageResponse<ProductResponse> findAll(PageQuery pageQuery, ProductFilter filter);

    ProductResponse update(UUID id, UpdateProductRequest request);

    void softDelete(UUID id);
}

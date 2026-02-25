package com.azenticsys.polaris.product.service;

import com.azenticsys.polaris.common.pagination.PageQuery;
import com.azenticsys.polaris.common.pagination.PageResponse;
import com.azenticsys.polaris.product.dto.CreateProductCategoryRequest;
import com.azenticsys.polaris.product.dto.ProductCategoryFilter;
import com.azenticsys.polaris.product.dto.ProductCategoryResponse;
import com.azenticsys.polaris.product.dto.UpdateProductCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryService {

    ProductCategoryResponse create(CreateProductCategoryRequest request);

    ProductCategoryResponse findById(UUID id);

    PageResponse<ProductCategoryResponse> findAll(PageQuery pageQuery, ProductCategoryFilter filter);

    List<ProductCategoryResponse> findRoots();

    List<ProductCategoryResponse> findChildren(UUID parentId);

    ProductCategoryResponse update(UUID id, UpdateProductCategoryRequest request);

    void softDelete(UUID id);
}

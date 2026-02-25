package com.azenticsys.polaris.product.repository;

import com.azenticsys.polaris.product.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, UUID>, JpaSpecificationExecutor<ProductCategory> {

    boolean existsByNameAndParentIsNull(String name);

    boolean existsByNameAndParent(String name, ProductCategory parent);

    boolean existsByNameAndParentIsNullAndIdNot(String name, UUID id);

    boolean existsByNameAndParentAndIdNot(String name, ProductCategory parent, UUID id);

    List<ProductCategory> findByParentIsNullAndDeletedAtIsNullOrderByDisplayOrderAsc();

    List<ProductCategory> findByParentAndDeletedAtIsNullOrderByDisplayOrderAsc(ProductCategory parent);
}

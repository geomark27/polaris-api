package com.azenticsys.polaris.product.repository;

import com.azenticsys.polaris.product.dto.ProductFilter;
import com.azenticsys.polaris.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA Criteria para filtrado dinámico de productos.
 * Siempre excluye registros con soft-delete (deletedAt IS NULL).
 */
public class ProductSpecification {

    private ProductSpecification() {}

    public static Specification<Product> withFilter(ProductFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre excluir soft-deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter != null) {
                if (filter.code() != null && !filter.code().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("code")),
                            "%" + filter.code().toLowerCase() + "%"
                    ));
                }
                if (filter.name() != null && !filter.name().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("name")),
                            "%" + filter.name().toLowerCase() + "%"
                    ));
                }
                if (filter.productType() != null && !filter.productType().isBlank()) {
                    predicates.add(cb.equal(
                            cb.upper(root.get("productType")),
                            filter.productType().toUpperCase()
                    ));
                }
                if (filter.categoryId() != null) {
                    predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
                }
                if (filter.isActive() != null) {
                    predicates.add(cb.equal(root.get("isActive"), filter.isActive()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.azenticsys.polaris.product.repository;

import com.azenticsys.polaris.product.dto.ProductCategoryFilter;
import com.azenticsys.polaris.product.entity.ProductCategory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA Criteria para filtrado dinámico de categorías de producto.
 * Siempre excluye registros con soft-delete (deletedAt IS NULL).
 */
public class ProductCategorySpecification {

    private ProductCategorySpecification() {}

    public static Specification<ProductCategory> withFilter(ProductCategoryFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre excluir soft-deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter != null) {
                if (filter.name() != null && !filter.name().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("name")),
                            "%" + filter.name().toLowerCase() + "%"
                    ));
                }
                if (filter.parentId() != null) {
                    predicates.add(cb.equal(root.get("parent").get("id"), filter.parentId()));
                }
                if (filter.level() != null) {
                    predicates.add(cb.equal(root.get("level"), filter.level()));
                }
                if (filter.isActive() != null) {
                    predicates.add(cb.equal(root.get("isActive"), filter.isActive()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

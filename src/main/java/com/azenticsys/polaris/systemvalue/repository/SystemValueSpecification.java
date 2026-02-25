package com.azenticsys.polaris.systemvalue.repository;

import com.azenticsys.polaris.systemvalue.dto.SystemValueFilter;
import com.azenticsys.polaris.systemvalue.entity.SystemValue;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA Criteria para filtrado dinámico de system values.
 * Siempre excluye registros con soft-delete (deletedAt IS NULL).
 */
public class SystemValueSpecification {

    private SystemValueSpecification() {}

    public static Specification<SystemValue> withFilter(SystemValueFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre excluir soft-deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter != null) {
                if (filter.catalogType() != null && !filter.catalogType().isBlank()) {
                    predicates.add(cb.equal(
                            cb.upper(root.get("catalogType")),
                            filter.catalogType().toUpperCase()
                    ));
                }
                if (filter.value() != null && !filter.value().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("value")),
                            "%" + filter.value().toLowerCase() + "%"
                    ));
                }
                if (filter.label() != null && !filter.label().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("label")),
                            "%" + filter.label().toLowerCase() + "%"
                    ));
                }
                if (filter.isActive() != null) {
                    predicates.add(cb.equal(root.get("isActive"), filter.isActive()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package com.azenticsys.polaris.user.repository;

import com.azenticsys.polaris.user.dto.UserFilter;
import com.azenticsys.polaris.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA Criteria para filtrado dinámico de usuarios.
 * Siempre excluye usuarios con soft-delete (deletedAt IS NULL).
 */
public class UserSpecification {

    private UserSpecification() {}

    public static Specification<User> withFilter(UserFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Siempre excluir soft-deleted
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (filter != null) {
                if (filter.username() != null && !filter.username().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("username")),
                            "%" + filter.username().toLowerCase() + "%"
                    ));
                }
                if (filter.email() != null && !filter.email().isBlank()) {
                    predicates.add(cb.like(
                            cb.lower(root.get("email")),
                            "%" + filter.email().toLowerCase() + "%"
                    ));
                }
                if (filter.phoneNumber() != null && !filter.phoneNumber().isBlank()) {
                    predicates.add(cb.like(
                            root.get("phoneNumber"),
                            "%" + filter.phoneNumber() + "%"
                    ));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

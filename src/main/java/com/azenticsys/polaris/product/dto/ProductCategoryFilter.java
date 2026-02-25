package com.azenticsys.polaris.product.dto;

import java.util.UUID;

/**
 * Filtros opcionales para búsqueda de categorías de producto.
 * Todos los campos son opcionales; null = sin filtro.
 */
public record ProductCategoryFilter(
        String name,
        UUID parentId,
        Integer level,
        Boolean isActive
) {}

package com.azenticsys.polaris.product.dto;

import java.util.UUID;

/**
 * Filtros opcionales para búsqueda de productos.
 * Todos los campos son opcionales; null = sin filtro.
 */
public record ProductFilter(
        String code,
        String name,
        String productType,
        UUID categoryId,
        Boolean isActive
) {}

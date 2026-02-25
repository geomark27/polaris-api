package com.azenticsys.polaris.systemvalue.dto;

/**
 * Filtros opcionales para búsqueda de system values.
 * Todos los campos son opcionales; null = sin filtro.
 */
public record SystemValueFilter(
        String catalogType,
        String value,
        String label,
        Boolean isActive
) {}

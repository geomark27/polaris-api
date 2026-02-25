package com.azenticsys.polaris.common.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Parámetros de paginación genéricos reutilizables por cualquier módulo.
 * - page: número de página (0-indexed), default 0
 * - size: registros por página, default 10, máximo 200
 * - sortBy: campo por el que ordenar, default "createdAt"
 * - sortDir: dirección de ordenamiento "asc" o "desc", default "desc"
 */
public record PageQuery(
        int page,
        int size,
        String sortBy,
        String sortDir
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE     = 200;
    private static final String DEFAULT_SORT_BY  = "createdAt";
    private static final String DEFAULT_SORT_DIR = "desc";

    public PageQuery {
        if (page < 0) page = DEFAULT_PAGE;
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
        if (sortBy == null || sortBy.isBlank()) sortBy = DEFAULT_SORT_BY;
        if (sortDir == null || sortDir.isBlank()) sortDir = DEFAULT_SORT_DIR;
    }

    /** Constructor con valores por defecto (usado cuando no se envían parámetros) */
    public static PageQuery defaults() {
        return new PageQuery(DEFAULT_PAGE, DEFAULT_SIZE, DEFAULT_SORT_BY, DEFAULT_SORT_DIR);
    }

    /** Convierte a Pageable de Spring Data */
    public Pageable toPageable() {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}

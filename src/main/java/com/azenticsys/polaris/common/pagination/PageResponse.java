package com.azenticsys.polaris.common.pagination;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope genérico de paginación reutilizable por cualquier módulo.
 *
 * @param data          Lista de elementos de la página actual
 * @param page          Número de página actual (0-indexed)
 * @param size          Tamaño de página solicitado
 * @param totalElements Total de registros en la BD (con filtros aplicados)
 * @param totalPages    Total de páginas
 * @param first         Si es la primera página
 * @param last          Si es la última página
 */
public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}

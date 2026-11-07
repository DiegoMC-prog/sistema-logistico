package com.tienda.backend.shared.application.model;

import java.util.List;
import java.util.function.Function;

/** Resultado paginado independiente de Spring Data, reutilizable por cualquier módulo. */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }

    public static int safePage(int page) {
        return Math.max(page, 0);
    }

    public static int safeSize(int size) {
        return size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }
}

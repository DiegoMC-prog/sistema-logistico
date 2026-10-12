package com.tienda.backend.application.dto.response;

public record CategoriaResponse(
        Long id,
        String nombre,
        Long totalProductos
) {
    public CategoriaResponse(Long id, String nombre) {
        this(id, nombre, 0L);
    }
}

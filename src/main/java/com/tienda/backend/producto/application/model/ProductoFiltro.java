package com.tienda.backend.producto.application.model;

import com.tienda.backend.producto.domain.model.EstadoProducto;

/** Criterios opcionales de búsqueda de productos; un campo nulo significa "sin filtrar por él". */
public record ProductoFiltro(
        String search,
        Long categoriaId,
        EstadoProducto estado,
        Boolean esPerecedero
) {
    public ProductoFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

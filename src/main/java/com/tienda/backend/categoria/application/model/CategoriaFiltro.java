package com.tienda.backend.categoria.application.model;

/** Criterios opcionales de búsqueda de categorías; un campo nulo significa "sin filtrar por él". */
public record CategoriaFiltro(String search) {
    public CategoriaFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

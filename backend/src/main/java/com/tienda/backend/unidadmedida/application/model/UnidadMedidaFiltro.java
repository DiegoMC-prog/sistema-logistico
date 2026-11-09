package com.tienda.backend.unidadmedida.application.model;

/** Criterios opcionales de búsqueda de unidades de medida; un campo nulo significa "sin filtrar por él". */
public record UnidadMedidaFiltro(String search) {
    public UnidadMedidaFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

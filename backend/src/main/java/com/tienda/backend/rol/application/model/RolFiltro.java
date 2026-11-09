package com.tienda.backend.rol.application.model;

/** Criterios opcionales de búsqueda de roles; un campo nulo significa "sin filtrar por él". */
public record RolFiltro(String search) {
    public RolFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

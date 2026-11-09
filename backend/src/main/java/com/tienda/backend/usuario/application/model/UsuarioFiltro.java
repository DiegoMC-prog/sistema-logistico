package com.tienda.backend.usuario.application.model;

import com.tienda.backend.usuario.domain.model.EstadoUsuario;

/** Criterios opcionales de búsqueda de usuarios; un campo nulo significa "sin filtrar por él". */
public record UsuarioFiltro(String search, EstadoUsuario estado, Long rolId) {
    public UsuarioFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

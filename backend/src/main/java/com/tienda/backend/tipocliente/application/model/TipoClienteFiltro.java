package com.tienda.backend.tipocliente.application.model;

import com.tienda.backend.tipocliente.domain.model.EstadoTipoCliente;

/** Criterios opcionales de búsqueda de tipos de cliente; un campo nulo significa "sin filtrar por él". */
public record TipoClienteFiltro(String search, EstadoTipoCliente estado) {
    public TipoClienteFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

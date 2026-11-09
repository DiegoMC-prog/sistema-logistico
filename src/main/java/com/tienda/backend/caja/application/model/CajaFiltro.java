package com.tienda.backend.caja.application.model;

import com.tienda.backend.caja.domain.model.EstadoCaja;

public record CajaFiltro(String search, Long depositoId, EstadoCaja estado) {
    public CajaFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

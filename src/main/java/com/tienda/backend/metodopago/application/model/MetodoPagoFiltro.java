package com.tienda.backend.metodopago.application.model;

import com.tienda.backend.metodopago.domain.model.EstadoMetodoPago;

/** Criterios opcionales de búsqueda de métodos de pago; un campo nulo significa "sin filtrar por él". */
public record MetodoPagoFiltro(String search, EstadoMetodoPago estado) {
    public MetodoPagoFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

package com.tienda.backend.precio.application.model;

import com.tienda.backend.precio.domain.model.EstadoPrecio;

/** Criterios opcionales para listar los precios de una presentación; un campo nulo significa "sin filtrar por él". */
public record PrecioFiltro(Long tipoClienteId, Boolean soloGenerales, EstadoPrecio estado) {
}

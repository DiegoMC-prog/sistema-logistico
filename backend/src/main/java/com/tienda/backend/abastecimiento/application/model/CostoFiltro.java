package com.tienda.backend.abastecimiento.application.model;

/** Filtro del histórico de costos; todos los campos son opcionales. */
public record CostoFiltro(Long proveedorId, Long productoId, String search) {
}

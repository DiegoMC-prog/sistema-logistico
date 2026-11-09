package com.tienda.backend.inventario.application.model;

import java.time.LocalDate;

/** Criterios opcionales de consulta de stock. {@code conStock}: true solo con stock, false solo agotados, null todos. */
public record StockFiltro(
        Long depositoId,
        Long productoId,
        Long loteId,
        String search,
        Boolean conStock,
        LocalDate venceHasta
) {
    public StockFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

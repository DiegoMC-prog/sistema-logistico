package com.tienda.backend.lote.application.model;

import java.time.LocalDate;

/** Criterios opcionales de búsqueda de lotes; un campo nulo significa "sin filtrar por él". */
public record LoteFiltro(Long productoId, String search, LocalDate venceDesde, LocalDate venceHasta) {
    public LoteFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

package com.tienda.backend.deposito.application.model;

import com.tienda.backend.deposito.domain.model.EstadoDeposito;

/** Criterios opcionales de búsqueda de depósitos; un campo nulo significa "sin filtrar por él". */
public record DepositoFiltro(String search, EstadoDeposito estado) {
    public DepositoFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

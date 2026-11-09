package com.tienda.backend.cliente.application.model;

import com.tienda.backend.cliente.domain.model.EstadoCliente;

/** Criterios opcionales de búsqueda de clientes; un campo nulo significa "sin filtrar por él". */
public record ClienteFiltro(String search, Long tipoClienteId, EstadoCliente estado) {
    public ClienteFiltro {
        search = (search == null || search.isBlank()) ? null : search.trim();
    }
}

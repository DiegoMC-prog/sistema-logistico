package com.tienda.backend.tipocliente.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.tipocliente.domain.model.TipoCliente;

public record TipoClienteResponse(
        Long id,
        String nombre,
        String estado
) {
    public static TipoClienteResponse from(TipoCliente t) {
        return new TipoClienteResponse(t.getId(), t.getNombre(), t.getEstado().name());
    }
}

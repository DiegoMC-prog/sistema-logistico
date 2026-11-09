package com.tienda.backend.deposito.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.deposito.domain.model.Deposito;

public record DepositoResponse(
        Long id,
        String nombre,
        String descripcion,
        String estado
) {
    public static DepositoResponse from(Deposito d) {
        return new DepositoResponse(d.getId(), d.getNombre(), d.getDescripcion(), d.getEstado().name());
    }
}

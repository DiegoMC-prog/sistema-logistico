package com.tienda.backend.unidadmedida.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;

public record UnidadMedidaResponse(
        Long id,
        String nombre,
        String abreviatura
) {
    public static UnidadMedidaResponse from(UnidadMedida u) {
        return new UnidadMedidaResponse(u.getId(), u.getNombre(), u.getAbreviatura());
    }
}

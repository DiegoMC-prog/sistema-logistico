package com.tienda.backend.caja.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.caja.domain.model.Caja;

public record CajaResponse(Long id, String nombre, Long depositoId, String estado) {
    public static CajaResponse from(Caja c) {
        return new CajaResponse(c.getId(), c.getNombre(), c.getDepositoId(), c.getEstado().name());
    }
}

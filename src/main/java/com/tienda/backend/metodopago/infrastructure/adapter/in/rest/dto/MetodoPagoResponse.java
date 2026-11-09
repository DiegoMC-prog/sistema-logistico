package com.tienda.backend.metodopago.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.metodopago.domain.model.MetodoPago;

public record MetodoPagoResponse(
        Long id,
        String nombre,
        String estado,
        /** false para métodos internos (el crédito de una devolución): no se ofrecen al cobrar ni al reembolsar. */
        boolean paraCobro
) {
    public static MetodoPagoResponse from(MetodoPago t) {
        return new MetodoPagoResponse(t.getId(), t.getNombre(), t.getEstado().name(), t.sirveParaCobrar());
    }
}

package com.tienda.backend.precio.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.precio.application.model.PrecioDetalle;
import com.tienda.backend.precio.domain.model.Precio;

import java.math.BigDecimal;

public record PrecioResponse(
        Long id,
        Long presentacionId,
        /** null cuando es el precio general. */
        TipoClienteResumen tipoCliente,
        BigDecimal cantidadMinima,
        BigDecimal precio,
        String estado
) {
    public record TipoClienteResumen(Long id, String nombre) {
    }

    public static PrecioResponse from(PrecioDetalle d) {
        Precio p = d.precio();
        TipoClienteResumen tipo = p.getTipoClienteId() == null ? null
                : new TipoClienteResumen(p.getTipoClienteId(), d.tipoClienteNombre());
        return new PrecioResponse(p.getId(), p.getPresentacionId(), tipo, p.getCantidadMinima(), p.getPrecio(),
                p.getEstado().name());
    }
}

package com.tienda.backend.precio.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.precio.application.model.PrecioDetalle;
import com.tienda.backend.precio.application.model.RecargoAplicado;
import com.tienda.backend.precio.domain.model.Precio;

import java.math.BigDecimal;

public record PrecioResponse(
        Long id,
        Long presentacionId,
        /** null cuando es el precio general. */
        TipoClienteResumen tipoCliente,
        BigDecimal cantidadMinima,
        /** Precio a cobrar: incluye el recargo por escasez cuando se resuelve el precio aplicable. */
        BigDecimal precio,
        String estado,
        /** Precio de la jerarquía, sin recargo. */
        BigDecimal precioBase,
        /** Recargo por escasez aplicado; null si no hay (y siempre null en el listado de precios). */
        RecargoResumen recargo
) {
    public record TipoClienteResumen(Long id, String nombre) {
    }

    /** @param porcentaje fracción: 0.15 = +15 % */
    public record RecargoResumen(Long id, String alcance, BigDecimal porcentaje, String motivo) {
    }

    public static PrecioResponse from(PrecioDetalle d) {
        Precio p = d.precio();
        TipoClienteResumen tipo = p.getTipoClienteId() == null ? null
                : new TipoClienteResumen(p.getTipoClienteId(), d.tipoClienteNombre());
        RecargoAplicado r = d.recargo();
        RecargoResumen recargo = r == null ? null
                : new RecargoResumen(r.id(), r.alcance().name(), r.porcentaje(), r.motivo());
        return new PrecioResponse(p.getId(), p.getPresentacionId(), tipo, p.getCantidadMinima(), d.precioFinal(),
                p.getEstado().name(), p.getPrecio(), recargo);
    }
}

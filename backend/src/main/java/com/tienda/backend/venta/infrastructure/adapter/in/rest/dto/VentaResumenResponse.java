package com.tienda.backend.venta.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.venta.domain.model.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fila de listado: sin líneas ni pagos. El detalle completo es {@link VentaResponse}. */
public record VentaResumenResponse(Long id, String numeroVenta, String estado, LocalDateTime fechaVenta, Long turnoId,
                                   Long usuarioId, Long clienteId, BigDecimal total) {
    public static VentaResumenResponse from(Venta v) {
        return new VentaResumenResponse(v.getId(), v.getNumeroVenta(), v.getEstado().name(), v.getFechaVenta(),
                v.getTurnoId(), v.getUsuarioId(), v.getClienteId(), v.getTotal());
    }
}

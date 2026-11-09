package com.tienda.backend.caja.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.caja.domain.model.MovimientoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaResponse(Long id, Long turnoId, Long usuarioId, String tipo, BigDecimal monto,
                                     String motivo, LocalDateTime fecha) {
    public static MovimientoCajaResponse from(MovimientoCaja m) {
        return new MovimientoCajaResponse(m.getId(), m.getTurnoId(), m.getUsuarioId(), m.getTipo().name(),
                m.getMonto(), m.getMotivo(), m.getFecha());
    }
}

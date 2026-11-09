package com.tienda.backend.caja.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.caja.application.model.TurnoDetalle;
import com.tienda.backend.caja.domain.model.TurnoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vista corta de un turno, para listados. El detalle con totales es {@link TurnoResumenResponse}. */
public record TurnoResponse(Long id, Long cajaId, String cajaNombre, Long usuarioId, String estado,
                            LocalDateTime fechaApertura, BigDecimal montoApertura, LocalDateTime fechaCierre,
                            BigDecimal montoCierreDeclarado, BigDecimal montoCierreEsperado) {
    public static TurnoResponse from(TurnoDetalle d) {
        TurnoCaja t = d.turno();
        return new TurnoResponse(t.getId(), t.getCajaId(), d.cajaNombre(), t.getUsuarioId(), t.getEstado().name(),
                t.getFechaApertura(), t.getMontoApertura(), t.getFechaCierre(), t.getMontoCierreDeclarado(),
                t.getMontoCierreEsperado());
    }
}

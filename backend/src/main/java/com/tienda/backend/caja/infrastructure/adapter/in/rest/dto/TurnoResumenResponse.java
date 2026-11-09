package com.tienda.backend.caja.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.caja.application.model.CobroPorMetodo;
import com.tienda.backend.caja.application.model.TurnoResumen;
import com.tienda.backend.caja.domain.model.TurnoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TurnoResumenResponse(
        Long id,
        Long cajaId,
        String cajaNombre,
        Long usuarioId,
        String estado,
        LocalDateTime fechaApertura,
        BigDecimal montoApertura,
        BigDecimal ingresos,
        BigDecimal egresos,
        List<CobroResponse> cobrosPorMetodo,
        BigDecimal efectivoCobrado,
        /** Efectivo devuelto a clientes en el turno (devoluciones reembolsadas en efectivo). */
        BigDecimal reembolsosEfectivo,
        BigDecimal efectivoEsperado,
        LocalDateTime fechaCierre,
        BigDecimal montoCierreDeclarado,
        /** declarado - esperado; null mientras el turno está abierto. */
        BigDecimal diferencia
) {
    public record CobroResponse(Long metodoPagoId, String metodoPago, BigDecimal total) {
        static CobroResponse from(CobroPorMetodo c) {
            return new CobroResponse(c.metodoPagoId(), c.metodoPagoNombre(), c.total());
        }
    }

    public static TurnoResumenResponse from(TurnoResumen r) {
        TurnoCaja t = r.turno();
        return new TurnoResumenResponse(t.getId(), t.getCajaId(), r.cajaNombre(), t.getUsuarioId(), t.getEstado().name(),
                t.getFechaApertura(), t.getMontoApertura(), r.ingresos(), r.egresos(),
                r.cobrosPorMetodo().stream().map(CobroResponse::from).toList(), r.efectivoCobrado(),
                r.reembolsosEfectivo(), r.efectivoEsperado(), t.getFechaCierre(), t.getMontoCierreDeclarado(), r.diferencia());
    }
}

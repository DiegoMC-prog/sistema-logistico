package com.tienda.backend.caja.application.port.out;

import com.tienda.backend.caja.application.model.CobroPorMetodo;

import java.util.List;

public interface ResumenCobrosTurnoPort {
    /** Total cobrado en el turno por método de pago, contando solo las ventas completadas (no las anuladas). */
    List<CobroPorMetodo> cobrosPorMetodo(Long turnoId);

    /** Efectivo devuelto a clientes en ese turno (reembolsos de devoluciones en EFECTIVO). */
    java.math.BigDecimal reembolsosEfectivo(Long turnoId);

    /** Abonos de deudas de crédito cobrados en EFECTIVO durante el turno (entran a la caja). */
    java.math.BigDecimal abonosEfectivo(Long turnoId);
}

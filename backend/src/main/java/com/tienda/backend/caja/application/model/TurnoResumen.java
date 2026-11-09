package com.tienda.backend.caja.application.model;

import com.tienda.backend.caja.domain.model.TurnoCaja;

import java.math.BigDecimal;
import java.util.List;

/**
 * Estado completo de un turno. Con el turno abierto, {@code efectivoEsperado} es el cálculo actual; con el turno cerrado
 * es el valor que quedó guardado al cerrar, y {@code diferencia} = declarado - esperado.
 */
public record TurnoResumen(
        TurnoCaja turno,
        String cajaNombre,
        BigDecimal ingresos,
        BigDecimal egresos,
        List<CobroPorMetodo> cobrosPorMetodo,
        BigDecimal efectivoCobrado,
        BigDecimal reembolsosEfectivo,
        BigDecimal abonosEfectivo,
        BigDecimal efectivoEsperado,
        BigDecimal diferencia
) {
}

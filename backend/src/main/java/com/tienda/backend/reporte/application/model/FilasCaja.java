package com.tienda.backend.reporte.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Filas de los reportes de caja. Importes con 2 decimales. */
public final class FilasCaja {
    private FilasCaja() {
    }

    /**
     * Cierre de un turno. {@code efectivoEsperado} = apertura + ingresos − egresos + ventas en efectivo + abonos de crédito en efectivo −
     * reembolsos en efectivo (un turno cerrado conserva el esperado que se calculó al cerrar).
     * {@code diferencia} = declarado − esperado (negativa = faltante); es nula mientras el turno está abierto.
     * {@code otrosMetodos} son los cobros que no fueron en efectivo (tarjeta, transferencia…).
     */
    public record Cierre(Long turnoId, Long cajaId, String caja, Long usuarioId, String cajero, String estado,
                         LocalDateTime fechaApertura, LocalDateTime fechaCierre, BigDecimal montoApertura,
                         long ventas, BigDecimal totalVentas, BigDecimal ventasEfectivo, BigDecimal otrosMetodos,
                         BigDecimal ingresos, BigDecimal egresos, BigDecimal reembolsosEfectivo,
                         BigDecimal efectivoEsperado, BigDecimal efectivoDeclarado, BigDecimal diferencia,
                         BigDecimal abonosEfectivo) {
        /** Sin abonos de crédito en efectivo (cero). */
        public Cierre(Long turnoId, Long cajaId, String caja, Long usuarioId, String cajero, String estado,
                      LocalDateTime fechaApertura, LocalDateTime fechaCierre, BigDecimal montoApertura,
                      long ventas, BigDecimal totalVentas, BigDecimal ventasEfectivo, BigDecimal otrosMetodos,
                      BigDecimal ingresos, BigDecimal egresos, BigDecimal reembolsosEfectivo,
                      BigDecimal efectivoEsperado, BigDecimal efectivoDeclarado, BigDecimal diferencia) {
            this(turnoId, cajaId, caja, usuarioId, cajero, estado, fechaApertura, fechaCierre, montoApertura, ventas,
                    totalVentas, ventasEfectivo, otrosMetodos, ingresos, egresos, reembolsosEfectivo, efectivoEsperado,
                    efectivoDeclarado, diferencia, BigDecimal.ZERO.setScale(2));
        }
    }

    /** Diferencias de caja de un cajero o de una caja: solo cuentan los turnos cerrados. */
    public record Diferencias(Long id, String nombre, long turnos, long turnosConDiferencia, BigDecimal sobrante,
                              BigDecimal faltante, BigDecimal neto) {
    }
}

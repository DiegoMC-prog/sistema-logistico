package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Modelo de lectura: una fila de stock (lote en un depósito) con los nombres para mostrar. */
public record StockDetalle(
        Long inventarioId,
        Long depositoId,
        String depositoNombre,
        Long productoId,
        String productoNombre,
        Long loteId,
        String numeroLote,
        LocalDate fechaVencimiento,
        BigDecimal stockActual,
        String unidadBase
) {
    public StockDetalle conUnidadBase(String abreviatura) {
        return new StockDetalle(inventarioId, depositoId, depositoNombre, productoId, productoNombre, loteId,
                numeroLote, fechaVencimiento, stockActual, abreviatura);
    }
}

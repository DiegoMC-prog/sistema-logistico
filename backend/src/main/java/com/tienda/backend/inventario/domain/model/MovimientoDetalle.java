package com.tienda.backend.inventario.domain.model;

import com.tienda.backend.inventario.domain.exception.MovimientoInvalidoException;

import java.math.BigDecimal;

/** Una línea de un movimiento: cuánto (en unidad base) de qué lote. */
public record MovimientoDetalle(Long productoId, Long loteId, BigDecimal cantidad) {
    public MovimientoDetalle {
        if (productoId == null || loteId == null) {
            throw new MovimientoInvalidoException("Cada línea del movimiento debe indicar producto y lote");
        }
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new MovimientoInvalidoException("La cantidad de cada línea debe ser mayor que cero");
        }
    }
}

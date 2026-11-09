package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;

/** Una línea de salida o traslado: un lote y cuánto. Si hay {@code presentacionId}, la cantidad es en esa presentación; si no, en unidad base. */
public record ItemMovimientoComando(Long loteId, Long presentacionId, BigDecimal cantidad) {
}

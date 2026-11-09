package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;

/**
 * Una línea de entrada: usa un lote existente ({@code loteId}) o crea uno nuevo ({@code lote}, con {@code productoId}).
 * Si hay {@code presentacionId}, la cantidad es en esa presentación; si no, en unidad base.
 */
public record ItemEntradaComando(
        Long productoId,
        Long loteId,
        NuevoLoteComando lote,
        Long presentacionId,
        BigDecimal cantidad
) {
}

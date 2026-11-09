package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;

/** Una línea de salida FEFO: cuánto de un producto (en la presentación indicada, o en unidad base si no hay). El lote lo elige el sistema. */
public record ItemFefoComando(Long productoId, Long presentacionId, BigDecimal cantidad) {
}

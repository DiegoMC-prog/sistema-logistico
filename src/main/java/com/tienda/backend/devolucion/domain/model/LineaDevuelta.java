package com.tienda.backend.devolucion.domain.model;

import java.math.BigDecimal;

/** Parte de una línea de venta que vuelve, de un lote concreto. La cantidad está en unidad base. */
public record LineaDevuelta(Long id, Long ventaDetalleId, Long loteId, BigDecimal cantidadBase, boolean reingresaStock) {
}

package com.tienda.backend.devolucion.application.model;

import java.math.BigDecimal;

/** Una línea de la venta que vuelve: cuánto (en la presentación vendida) y si el producto reingresa al stock. */
public record ItemDevueltoComando(Long ventaDetalleId, BigDecimal cantidad, boolean reingresaStock) {
}

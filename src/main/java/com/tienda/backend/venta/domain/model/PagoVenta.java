package com.tienda.backend.venta.domain.model;

import java.math.BigDecimal;

/** Pago de una venta. En efectivo, el monto es el neto: lo recibido menos el vuelto. */
public record PagoVenta(Long id, Long metodoPagoId, BigDecimal monto, String referencia) {
}

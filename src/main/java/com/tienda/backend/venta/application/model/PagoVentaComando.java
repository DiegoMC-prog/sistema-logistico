package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;

/** Un pago recibido. En efectivo se puede recibir más que lo debido: la diferencia es el vuelto. */
public record PagoVentaComando(Long metodoPagoId, BigDecimal monto, String referencia) {
}

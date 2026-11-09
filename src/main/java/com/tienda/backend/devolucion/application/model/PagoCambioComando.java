package com.tienda.backend.devolucion.application.model;

import java.math.BigDecimal;

/** Pago de la diferencia en un cambio (cuando lo nuevo vale más que lo devuelto). */
public record PagoCambioComando(Long metodoPagoId, BigDecimal monto, String referencia) {
}

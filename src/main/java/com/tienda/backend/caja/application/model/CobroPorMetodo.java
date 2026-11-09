package com.tienda.backend.caja.application.model;

import java.math.BigDecimal;

/** Total cobrado en un turno con un método de pago (solo ventas completadas). */
public record CobroPorMetodo(Long metodoPagoId, String metodoPagoNombre, BigDecimal total) {
}

package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;

/** Una línea pedida. Con {@code precioUnitario} se cobra ese precio en vez del de la lista (requiere permiso y motivo). */
public record ItemVentaComando(Long presentacionId, BigDecimal cantidad, BigDecimal precioUnitario, String motivoPrecio) {
}

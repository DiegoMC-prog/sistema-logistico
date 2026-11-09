package com.tienda.backend.devolucion.application.model;

import java.math.BigDecimal;

/** Una línea de lo que se lleva el cliente en un cambio. Con {@code precioUnitario} se cobra ese precio (requiere permiso). */
public record ItemNuevoComando(Long presentacionId, BigDecimal cantidad, BigDecimal precioUnitario, String motivoPrecio) {
}

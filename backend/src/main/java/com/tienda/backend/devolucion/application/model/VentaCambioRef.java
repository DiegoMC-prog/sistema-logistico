package com.tienda.backend.devolucion.application.model;

import java.math.BigDecimal;

public record VentaCambioRef(Long id, String numeroVenta, BigDecimal total) {
}

package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;

public record ItemStock(Long productoId, Long presentacionId, BigDecimal cantidad) {
}

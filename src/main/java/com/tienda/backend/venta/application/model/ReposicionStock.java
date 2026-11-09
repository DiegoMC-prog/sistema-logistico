package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;

public record ReposicionStock(Long productoId, Long loteId, BigDecimal cantidadBase) {
}

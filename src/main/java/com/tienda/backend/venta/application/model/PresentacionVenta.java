package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;

public record PresentacionVenta(Long presentacionId, Long productoId, String productoNombre, boolean productoActivo,
                                BigDecimal factorConversion) {
}

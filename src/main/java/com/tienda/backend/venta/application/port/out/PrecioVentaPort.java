package com.tienda.backend.venta.application.port.out;

import java.math.BigDecimal;

public interface PrecioVentaPort {
    /** Precio de lista para esa presentación, cantidad y tipo de cliente (null = sin tipo). */
    BigDecimal precioAplicable(Long presentacionId, BigDecimal cantidad, Long tipoClienteId);
}

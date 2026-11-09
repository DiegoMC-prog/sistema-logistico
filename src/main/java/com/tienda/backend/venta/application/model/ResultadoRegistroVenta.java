package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;

/**
 * {@code creada} es false cuando era un reintento con la misma clave de idempotencia y se devolvió la venta existente.
 * {@code creditoAplicado} es lo que se cubrió con el crédito de una devolución (cero en una venta normal).
 */
public record ResultadoRegistroVenta(VentaVista vista, boolean creada, BigDecimal creditoAplicado) {
    public ResultadoRegistroVenta(VentaVista vista, boolean creada) {
        this(vista, creada, BigDecimal.ZERO);
    }
}

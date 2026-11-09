package com.tienda.backend.venta.domain.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Línea de una venta. {@code precioUnitario} es el de lista (o el manual); {@code descuento} es el monto rebajado por
 * vencimiento del lote; {@code subtotal} = precio x cantidad - descuento. {@code cantidad} está en la presentación vendida.
 */
public record LineaVenta(
        Long id,
        Long productoId,
        Long presentacionId,
        BigDecimal cantidad,
        BigDecimal factorConversion,
        BigDecimal precioUnitario,
        BigDecimal descuento,
        BigDecimal subtotal,
        boolean esPrecioModificado,
        String motivoPrecio,
        List<LoteVendido> lotes
) {
    public LineaVenta {
        lotes = List.copyOf(lotes);
    }

    /** Importe antes del descuento. */
    public BigDecimal bruto() {
        return subtotal.add(descuento);
    }
}

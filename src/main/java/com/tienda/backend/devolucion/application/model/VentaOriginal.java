package com.tienda.backend.devolucion.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** La venta que se devuelve, con lo necesario para repartir la devolución entre sus líneas y lotes. */
public record VentaOriginal(Long id, String numeroVenta, boolean anulada, Long clienteId, Long depositoId,
                            LocalDateTime fechaVenta, List<LineaOriginal> lineas) {

    public record LineaOriginal(Long ventaDetalleId, Long productoId, Long presentacionId, BigDecimal cantidad,
                                BigDecimal factorConversion, BigDecimal subtotal, List<LoteOriginal> lotes) {
    }

    public record LoteOriginal(Long loteId, BigDecimal cantidadBase) {
    }
}

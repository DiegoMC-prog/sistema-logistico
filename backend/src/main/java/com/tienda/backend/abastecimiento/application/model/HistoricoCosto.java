package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un punto del histórico de costos: una línea de un pedido confirmado.
 *
 * @param variacionAnterior variación contra la compra anterior del mismo producto al mismo proveedor (null si es la primera)
 */
public record HistoricoCosto(
        Long detalleId,
        Long pedidoId,
        String numeroPedido,
        String estadoPedido,
        Long proveedorId,
        String proveedor,
        Long productoId,
        String producto,
        String categoria,
        LocalDate fecha,
        BigDecimal costoUnitario,
        BigDecimal cantidad,
        BigDecimal variacionAnterior
) {
    public HistoricoCosto conVariacion(BigDecimal variacion) {
        return new HistoricoCosto(detalleId, pedidoId, numeroPedido, estadoPedido, proveedorId, proveedor, productoId,
                producto, categoria, fecha, costoUnitario, cantidad, variacion);
    }
}

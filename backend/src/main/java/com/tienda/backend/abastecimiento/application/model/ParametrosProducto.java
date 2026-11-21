package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;

/** Parámetros de reorden de un producto: los propios (null = usa el global) y los que efectivamente se aplican. */
public record ParametrosProducto(
        Long productoId,
        String producto,
        BigDecimal costoPedido,
        BigDecimal costoAlmacenPct,
        BigDecimal nivelServicio,
        Long proveedorId,
        String proveedor,
        boolean activo,
        BigDecimal costoPedidoEfectivo,
        BigDecimal costoAlmacenPctEfectivo,
        BigDecimal nivelServicioEfectivo
) {
}

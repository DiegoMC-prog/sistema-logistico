package com.tienda.backend.pedido.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Datos para crear o editar un pedido en borrador. Las cantidades van en unidad base del producto. */
public record PedidoComando(
        Long proveedorId,
        Long depositoDestinoId,
        LocalDate fechaEntregaEstimada,
        String observacion,
        List<Item> items
) {
    public record Item(Long productoId, BigDecimal cantidad, BigDecimal costoUnitario) {
    }
}

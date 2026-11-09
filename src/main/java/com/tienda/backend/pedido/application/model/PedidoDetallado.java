package com.tienda.backend.pedido.application.model;

import com.tienda.backend.pedido.domain.model.PedidoProveedor;

import java.util.Map;

/** Modelo de lectura: el pedido con los nombres de proveedor, depósito y productos. */
public record PedidoDetallado(
        PedidoProveedor pedido,
        String proveedorNombre,
        String depositoNombre,
        Map<Long, String> productoNombres
) {
}

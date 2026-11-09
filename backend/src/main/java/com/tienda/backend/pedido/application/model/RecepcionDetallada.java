package com.tienda.backend.pedido.application.model;

import com.tienda.backend.pedido.domain.model.EstadoPedido;
import com.tienda.backend.pedido.domain.model.Recepcion;

import java.util.Map;

/**
 * Modelo de lectura de una recepción: con el número y estado actual del pedido, el usuario que la registró y, por
 * línea, el producto y el número del lote creado.
 */
public record RecepcionDetallada(
        Recepcion recepcion,
        String numeroPedido,
        EstadoPedido estadoPedido,
        String usuarioNombre,
        Map<Long, Long> productoPorLinea,
        Map<Long, String> productoNombres,
        Map<Long, String> numerosLote
) {
}

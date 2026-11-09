package com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.pedido.application.model.RecepcionDetallada;
import com.tienda.backend.pedido.domain.model.Recepcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RecepcionResponse(
        Long id,
        Long pedidoId,
        String numeroPedido,
        /** Estado del pedido después de esta recepción (o el actual, en el listado). */
        String estadoPedido,
        LocalDateTime fechaRecepcion,
        Usuario usuario,
        String numeroGuia,
        String observacion,
        List<Linea> items
) {
    public record Usuario(Long id, String nombre) {
    }

    public record Producto(Long id, String nombre) {
    }

    public record Lote(Long id, String numeroLote) {
    }

    /** Cantidad en unidad base; {@code lineaId} es la línea del pedido a la que corresponde. */
    public record Linea(Long lineaId, Producto producto, Lote lote, BigDecimal cantidad) {
    }

    public static RecepcionResponse from(RecepcionDetallada d) {
        Recepcion r = d.recepcion();
        return new RecepcionResponse(r.id(), r.pedidoId(), d.numeroPedido(), d.estadoPedido().name(),
                r.fechaRecepcion(), new Usuario(r.usuarioId(), d.usuarioNombre()), r.numeroGuia(), r.observacion(),
                r.lineas().stream().map(l -> {
                    Long productoId = d.productoPorLinea().get(l.pedidoDetalleId());
                    return new Linea(l.pedidoDetalleId(), new Producto(productoId, d.productoNombres().get(productoId)),
                            new Lote(l.loteId(), d.numerosLote().get(l.loteId())), l.cantidad());
                }).toList());
    }
}

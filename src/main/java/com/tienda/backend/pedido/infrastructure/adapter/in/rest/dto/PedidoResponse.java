package com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.pedido.application.model.PedidoDetallado;
import com.tienda.backend.pedido.domain.model.LineaPedido;
import com.tienda.backend.pedido.domain.model.PedidoProveedor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoResponse(
        Long id,
        String numeroPedido,
        String estado,
        Resumen proveedor,
        Resumen depositoDestino,
        LocalDate fechaPedido,
        LocalDate fechaEntregaEstimada,
        String observacion,
        BigDecimal total,
        List<Linea> items
) {
    public record Resumen(Long id, String nombre) {
    }

    /** Cantidades en unidad base del producto. {@code nombre} del producto es null en los listados. */
    public record Linea(Long id, Resumen producto, BigDecimal cantidadPedida, BigDecimal cantidadRecibida,
                        BigDecimal cantidadPendiente, BigDecimal costoUnitario, BigDecimal subtotal) {
    }

    public static PedidoResponse from(PedidoDetallado d) {
        PedidoProveedor p = d.pedido();
        return new PedidoResponse(p.getId(), p.getNumeroPedido(), p.getEstado().name(),
                new Resumen(p.getProveedorId(), d.proveedorNombre()),
                new Resumen(p.getDepositoDestinoId(), d.depositoNombre()),
                p.getFechaPedido(), p.getFechaEntregaEstimada(), p.getObservacion(), p.getTotal(),
                p.getLineas().stream().map(l -> linea(l, d)).toList());
    }

    private static Linea linea(LineaPedido l, PedidoDetallado d) {
        return new Linea(l.getId(), new Resumen(l.getProductoId(), d.productoNombres().get(l.getProductoId())),
                l.getCantidadPedida(), l.getCantidadRecibida(), l.pendiente(), l.getCostoUnitario(), l.subtotal());
    }
}

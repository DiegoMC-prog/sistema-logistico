package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import com.tienda.backend.pedido.domain.model.EstadoPedido;
import com.tienda.backend.pedido.domain.model.LineaPedido;
import com.tienda.backend.pedido.domain.model.PedidoProveedor;
import com.tienda.backend.pedido.domain.model.Recepcion;
import com.tienda.backend.pedido.domain.model.Recepcion.LineaRecepcion;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PedidoPersistenceMapper {

    public PedidoEntity toEntity(PedidoProveedor p) {
        return PedidoEntity.builder()
                .id(p.getId())
                .numeroPedido(p.getNumeroPedido())
                .proveedorId(p.getProveedorId())
                .depositoDestinoId(p.getDepositoDestinoId())
                .estado(p.getEstado().name())
                .fechaPedido(p.getFechaPedido())
                .fechaEntregaEstimada(p.getFechaEntregaEstimada())
                .observacion(p.getObservacion())
                .total(p.getTotal())
                .version(p.getVersion())
                .build();
    }

    public PedidoDetalleEntity toEntity(Long pedidoId, LineaPedido l) {
        return PedidoDetalleEntity.builder()
                .id(l.getId())
                .pedidoId(pedidoId)
                .productoId(l.getProductoId())
                .cantidadPedida(l.getCantidadPedida())
                .cantidadRecibida(l.getCantidadRecibida())
                .costoUnitario(l.getCostoUnitario())
                .build();
    }

    public PedidoProveedor toDomain(PedidoEntity e, List<PedidoDetalleEntity> detalles) {
        List<LineaPedido> lineas = detalles.stream()
                .map(d -> new LineaPedido(d.getId(), d.getProductoId(), d.getCantidadPedida(),
                        d.getCantidadRecibida(), d.getCostoUnitario()))
                .toList();
        return new PedidoProveedor(e.getId(), e.getNumeroPedido(), e.getProveedorId(), e.getDepositoDestinoId(),
                EstadoPedido.valueOf(e.getEstado()), e.getFechaPedido(), e.getFechaEntregaEstimada(),
                e.getObservacion(), e.getVersion(), lineas);
    }

    public Recepcion toDomain(RecepcionEntity e, List<RecepcionDetalleEntity> detalles) {
        List<LineaRecepcion> lineas = detalles.stream()
                .map(d -> new LineaRecepcion(d.getId(), d.getPedidoDetalleId(), d.getLoteId(), d.getCantidad()))
                .toList();
        return new Recepcion(e.getId(), e.getPedidoId(), e.getUsuarioId(), e.getFechaRecepcion(), e.getNumeroGuia(),
                e.getObservacion(), lineas);
    }
}

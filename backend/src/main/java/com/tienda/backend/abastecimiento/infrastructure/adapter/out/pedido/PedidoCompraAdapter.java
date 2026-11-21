package com.tienda.backend.abastecimiento.infrastructure.adapter.out.pedido;

import com.tienda.backend.abastecimiento.application.model.LineaCompra;
import com.tienda.backend.abastecimiento.application.model.PedidoGenerado;
import com.tienda.backend.abastecimiento.application.port.out.PedidoCompraPort;
import com.tienda.backend.pedido.application.model.PedidoComando;
import com.tienda.backend.pedido.application.model.PedidoDetallado;
import com.tienda.backend.pedido.application.port.in.GestionarPedidoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Delega en el caso de uso del módulo de pedidos: valida proveedor, depósito y productos como en la pantalla. */
@Component
@RequiredArgsConstructor
public class PedidoCompraAdapter implements PedidoCompraPort {

    private final GestionarPedidoUseCase gestionarPedidoUseCase;

    @Override
    public PedidoGenerado crearBorrador(Long proveedorId, Long depositoDestinoId, String observacion,
                                        List<LineaCompra> lineas) {
        PedidoComando comando = new PedidoComando(proveedorId, depositoDestinoId, null, observacion,
                lineas.stream()
                        .map(l -> new PedidoComando.Item(l.productoId(), l.cantidad(), l.costoUnitario()))
                        .toList());
        PedidoDetallado creado = gestionarPedidoUseCase.crear(comando);
        return new PedidoGenerado(creado.pedido().getId(), creado.pedido().getNumeroPedido(), proveedorId,
                creado.proveedorNombre(), creado.pedido().getLineas().size(), creado.pedido().getTotal());
    }
}

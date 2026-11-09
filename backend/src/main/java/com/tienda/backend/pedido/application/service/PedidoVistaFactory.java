package com.tienda.backend.pedido.application.service;

import com.tienda.backend.pedido.application.model.PedidoDetallado;
import com.tienda.backend.pedido.application.model.RecepcionDetallada;
import com.tienda.backend.pedido.application.port.out.ReferenciasPedidoPort;
import com.tienda.backend.pedido.domain.model.LineaPedido;
import com.tienda.backend.pedido.domain.model.PedidoProveedor;
import com.tienda.backend.pedido.domain.model.Recepcion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Agrega a los pedidos y recepciones los nombres de proveedor, depósito, producto, usuario y lote. */
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PedidoVistaFactory {
    private static final String ELIMINADO = "(eliminado)";

    private final ReferenciasPedidoPort referencias;

    /** Vista completa de un pedido, con los nombres de producto de cada línea. */
    public PedidoDetallado construir(PedidoProveedor pedido) {
        Set<Long> productoIds = pedido.getLineas().stream().map(LineaPedido::getProductoId)
                .collect(Collectors.toSet());
        return new PedidoDetallado(pedido,
                referencias.nombresProveedores(List.of(pedido.getProveedorId()))
                        .getOrDefault(pedido.getProveedorId(), ELIMINADO),
                referencias.nombresDepositos(List.of(pedido.getDepositoDestinoId()))
                        .getOrDefault(pedido.getDepositoDestinoId(), ELIMINADO),
                referencias.nombresProductos(productoIds));
    }

    /** Vista de un listado: proveedor y depósito con una consulta por tipo; sin nombres de producto. */
    public List<PedidoDetallado> construirCabeceras(List<PedidoProveedor> pedidos) {
        if (pedidos.isEmpty()) {
            return List.of();
        }
        Set<Long> proveedorIds = pedidos.stream().map(PedidoProveedor::getProveedorId).collect(Collectors.toSet());
        Set<Long> depositoIds = pedidos.stream().map(PedidoProveedor::getDepositoDestinoId).collect(Collectors.toSet());
        Map<Long, String> proveedores = referencias.nombresProveedores(proveedorIds);
        Map<Long, String> depositos = referencias.nombresDepositos(depositoIds);
        return pedidos.stream()
                .map(p -> new PedidoDetallado(p,
                        proveedores.getOrDefault(p.getProveedorId(), ELIMINADO),
                        depositos.getOrDefault(p.getDepositoDestinoId(), ELIMINADO),
                        Map.of()))
                .toList();
    }

    public RecepcionDetallada construir(PedidoProveedor pedido, Recepcion recepcion) {
        return construir(pedido, List.of(recepcion)).get(0);
    }

    public List<RecepcionDetallada> construir(PedidoProveedor pedido, List<Recepcion> recepciones) {
        if (recepciones.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> productoPorLinea = new HashMap<>();
        pedido.getLineas().forEach(l -> productoPorLinea.put(l.getId(), l.getProductoId()));

        Set<Long> usuarioIds = new HashSet<>();
        Set<Long> loteIds = new HashSet<>();
        for (Recepcion r : recepciones) {
            usuarioIds.add(r.usuarioId());
            r.lineas().forEach(l -> loteIds.add(l.loteId()));
        }
        Map<Long, String> usuarios = referencias.nombresUsuarios(usuarioIds);
        Map<Long, String> productos = referencias.nombresProductos(new HashSet<>(productoPorLinea.values()));
        Map<Long, String> lotes = referencias.numerosLote(loteIds);

        return recepciones.stream()
                .map(r -> new RecepcionDetallada(r, pedido.getNumeroPedido(), pedido.getEstado(),
                        usuarios.getOrDefault(r.usuarioId(), ELIMINADO), productoPorLinea, productos, lotes))
                .toList();
    }
}

package com.tienda.backend.pedido.application.port.out;

import com.tienda.backend.pedido.application.model.PedidoFiltro;
import com.tienda.backend.pedido.domain.model.PedidoProveedor;
import com.tienda.backend.pedido.domain.model.Recepcion;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {
    PedidoProveedor save(PedidoProveedor pedido);
    Optional<PedidoProveedor> findById(Long id);

    /** Con bloqueo de escritura: dos recepciones simultáneas del mismo pedido se atienden una tras otra. */
    Optional<PedidoProveedor> findByIdParaActualizar(Long id);

    /** El listado trae las cabeceras con sus líneas. */
    PageResult<PedidoProveedor> findAll(PedidoFiltro filtro, int page, int size);

    void deleteById(Long id);

    Recepcion saveRecepcion(Recepcion recepcion);
    List<Recepcion> findRecepcionesByPedidoId(Long pedidoId);
}

package com.tienda.backend.pedido.application.port.in;

import com.tienda.backend.pedido.application.model.PedidoDetallado;
import com.tienda.backend.pedido.application.model.PedidoFiltro;
import com.tienda.backend.pedido.application.model.RecepcionDetallada;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.List;

public interface ConsultarPedidoUseCase {
    /** Listado con proveedor y depósito; las líneas vienen sin nombres de producto (usar {@link #obtener}). */
    PageResult<PedidoDetallado> listar(PedidoFiltro filtro, int page, int size);

    PedidoDetallado obtener(Long id);

    List<RecepcionDetallada> listarRecepciones(Long pedidoId);
}

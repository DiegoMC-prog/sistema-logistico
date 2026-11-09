package com.tienda.backend.pedido.application.port.in;

import com.tienda.backend.pedido.application.model.PedidoComando;
import com.tienda.backend.pedido.application.model.PedidoDetallado;

public interface GestionarPedidoUseCase {
    /** Crea un pedido en BORRADOR. */
    PedidoDetallado crear(PedidoComando comando);

    /** Reemplaza los datos y las líneas de un pedido en BORRADOR. */
    PedidoDetallado editar(Long id, PedidoComando comando);

    /** BORRADOR a ENVIADO. */
    PedidoDetallado enviar(Long id);

    /** Cancela un pedido en BORRADOR o ENVIADO (sin recepciones). */
    PedidoDetallado cancelar(Long id);

    /** Elimina un pedido en BORRADOR. */
    void eliminar(Long id);
}

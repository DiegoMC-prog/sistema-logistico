package com.tienda.backend.pedido.application.port.in;

import com.tienda.backend.pedido.application.model.RecepcionComando;
import com.tienda.backend.pedido.application.model.RecepcionDetallada;

public interface RegistrarRecepcionUseCase {
    /**
     * Recibe mercadería de un pedido ENVIADO o RECIBIDO_PARCIAL: crea un lote por línea, ingresa el stock al depósito
     * del pedido y deja el pedido en RECIBIDO_PARCIAL o RECIBIDO. Todo en una sola transacción.
     */
    RecepcionDetallada registrar(RecepcionComando comando);
}

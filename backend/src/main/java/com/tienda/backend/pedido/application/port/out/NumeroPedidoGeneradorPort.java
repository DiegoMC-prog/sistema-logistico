package com.tienda.backend.pedido.application.port.out;

public interface NumeroPedidoGeneradorPort {
    /** Siguiente número de pedido, ya formateado y único. */
    String siguiente();
}

package com.tienda.backend.metodopago.application.port.out;

public interface UsoMetodoPagoConsultaPort {
    /** true si algún cliente, precio o venta usa este método de pago. */
    boolean estaEnUso(Long metodoPagoId);
}

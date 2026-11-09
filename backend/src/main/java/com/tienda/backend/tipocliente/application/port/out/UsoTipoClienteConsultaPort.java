package com.tienda.backend.tipocliente.application.port.out;

public interface UsoTipoClienteConsultaPort {
    /** true si algún cliente, precio o venta usa este tipo de cliente. */
    boolean estaEnUso(Long tipoClienteId);
}

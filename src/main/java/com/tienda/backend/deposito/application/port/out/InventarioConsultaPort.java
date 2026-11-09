package com.tienda.backend.deposito.application.port.out;

public interface InventarioConsultaPort {
    /** true si el depósito tiene stock mayor que cero de algún lote. */
    boolean tieneStock(Long depositoId);
}

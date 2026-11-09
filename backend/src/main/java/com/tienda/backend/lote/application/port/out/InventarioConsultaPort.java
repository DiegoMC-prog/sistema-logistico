package com.tienda.backend.lote.application.port.out;

public interface InventarioConsultaPort {
    /** true si el lote tiene stock mayor que cero en algún depósito. */
    boolean tieneStock(Long loteId);
}

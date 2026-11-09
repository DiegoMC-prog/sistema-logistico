package com.tienda.backend.cliente.application.port.out;

public interface VentaConsultaPort {
    /** true si el cliente tiene alguna venta registrada (completada o anulada). */
    boolean clienteTieneVentas(Long clienteId);
}

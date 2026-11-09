package com.tienda.backend.venta.application.port.out;

public interface VentaDevolucionConsultaPort {
    boolean tieneDevoluciones(Long ventaId);

    /** true si la venta es la que se llevó el cliente en un cambio. */
    boolean esVentaDeCambio(Long ventaId);
}

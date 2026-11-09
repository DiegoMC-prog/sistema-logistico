package com.tienda.backend.devolucion.application.port.out;

import com.tienda.backend.devolucion.application.model.ItemNuevoComando;
import com.tienda.backend.devolucion.application.model.PagoCambioComando;
import com.tienda.backend.devolucion.application.model.ResultadoVentaCambio;
import com.tienda.backend.devolucion.application.model.VentaCambioRef;
import com.tienda.backend.devolucion.application.model.VentaOriginal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VentaDevolucionPort {
    /** Lee la venta bloqueándola: dos devoluciones simultáneas de la misma venta, o una contra su anulación, se serializan. */
    Optional<VentaOriginal> bloquearVenta(Long ventaId);

    Optional<VentaCambioRef> resumenVenta(Long ventaId);

    /**
     * Registra la venta nueva de un cambio dentro de la transacción en curso, aplicando {@code credito} antes de cobrar
     * la diferencia con {@code pagos}.
     */
    ResultadoVentaCambio registrarVentaDeCambio(Long clienteId, List<ItemNuevoComando> items, List<PagoCambioComando> pagos,
                                                BigDecimal credito);
}

package com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.devolucion.application.model.DevolucionVista;

import java.math.BigDecimal;

/** Resultado de un cambio: la devolución, la venta nueva y cómo se saldó la diferencia. */
public record CambioResponse(
        DevolucionResponse devolucion,
        /** Crédito de lo devuelto que cubrió la venta nueva. */
        BigDecimal creditoAplicado,
        /** Lo que el cliente pagó de más por lo nuevo (con los pagos enviados); cero si el crédito alcanzó. */
        BigDecimal diferenciaCobrada,
        /** Efectivo que se le entregó al cliente porque lo devuelto valía más que lo nuevo; cero en el caso contrario. */
        BigDecimal saldoEntregadoEnEfectivo
) {
    public static CambioResponse from(DevolucionVista vista) {
        BigDecimal credito = vista.devolucion().getMontoReembolsado();
        BigDecimal total = vista.ventaCambio() == null ? BigDecimal.ZERO : vista.ventaCambio().total();
        BigDecimal aplicado = credito.min(total);
        return new CambioResponse(DevolucionResponse.from(vista), aplicado, total.subtract(aplicado), credito.subtract(aplicado));
    }
}

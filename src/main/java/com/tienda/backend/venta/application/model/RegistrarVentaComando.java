package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@code idempotencyKey} es opcional: con ella, reintentar la misma petición devuelve la misma venta en vez de duplicarla.
 * {@code creditoDisponible} solo lo fija el cambio de una devolución (uso interno, nunca desde la API): es el crédito
 * que se aplica a esta venta antes de cobrar la diferencia.
 */
public record RegistrarVentaComando(Long clienteId, List<ItemVentaComando> items, List<PagoVentaComando> pagos,
                                    String idempotencyKey, BigDecimal creditoDisponible) {
    public RegistrarVentaComando {
        items = items == null ? List.of() : List.copyOf(items);
        pagos = pagos == null ? List.of() : List.copyOf(pagos);
        creditoDisponible = creditoDisponible == null ? BigDecimal.ZERO : creditoDisponible;
    }

    /** Venta normal, sin crédito. */
    public RegistrarVentaComando(Long clienteId, List<ItemVentaComando> items, List<PagoVentaComando> pagos,
                                 String idempotencyKey) {
        this(clienteId, items, pagos, idempotencyKey, null);
    }
}

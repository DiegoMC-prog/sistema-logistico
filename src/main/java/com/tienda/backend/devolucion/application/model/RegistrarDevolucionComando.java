package com.tienda.backend.devolucion.application.model;

import java.util.List;

public record RegistrarDevolucionComando(Long ventaId, String motivo, boolean reembolsar, Long metodoPagoId,
                                         List<ItemDevueltoComando> items, String idempotencyKey) {
    public RegistrarDevolucionComando {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

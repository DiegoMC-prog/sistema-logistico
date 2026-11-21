package com.tienda.backend.devolucion.application.model;

import com.tienda.backend.shared.application.model.CredencialesSupervisor;
import java.util.List;

public record RegistrarDevolucionComando(Long ventaId, String motivo, boolean reembolsar, Long metodoPagoId,
                                         List<ItemDevueltoComando> items, String idempotencyKey,
                                         CredencialesSupervisor autorizacion) {
    public RegistrarDevolucionComando {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

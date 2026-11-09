package com.tienda.backend.devolucion.application.model;

import java.util.List;

public record RegistrarCambioComando(Long ventaId, String motivo, List<ItemDevueltoComando> devueltos,
                                     List<ItemNuevoComando> nuevos, List<PagoCambioComando> pagos, String idempotencyKey) {
    public RegistrarCambioComando {
        devueltos = devueltos == null ? List.of() : List.copyOf(devueltos);
        nuevos = nuevos == null ? List.of() : List.copyOf(nuevos);
        pagos = pagos == null ? List.of() : List.copyOf(pagos);
    }
}

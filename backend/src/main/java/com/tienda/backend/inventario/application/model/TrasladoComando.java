package com.tienda.backend.inventario.application.model;

import java.util.List;

public record TrasladoComando(
        Long depositoOrigenId,
        Long depositoDestinoId,
        String observacion,
        List<ItemMovimientoComando> items
) {
}

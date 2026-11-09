package com.tienda.backend.inventario.application.model;

import java.util.List;

/** {@code referenciaTipo}/{@code referenciaId} los fijan solo los casos de uso internos (p. ej. una venta). */
public record SalidaComando(
        Long depositoId,
        String motivo,
        List<ItemMovimientoComando> items,
        String referenciaTipo,
        Long referenciaId
) {
}

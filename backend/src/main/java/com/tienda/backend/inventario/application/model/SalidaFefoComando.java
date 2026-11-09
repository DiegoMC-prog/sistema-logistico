package com.tienda.backend.inventario.application.model;

import java.util.List;

/** Salida de stock donde el sistema reparte cada línea entre los lotes por FEFO (vence primero, sale primero). */
public record SalidaFefoComando(
        Long depositoId,
        String motivo,
        List<ItemFefoComando> items,
        String referenciaTipo,
        Long referenciaId
) {
}

package com.tienda.backend.inventario.application.model;

import java.util.List;

/** {@code referenciaTipo}/{@code referenciaId} los fijan solo los casos de uso internos (p. ej. recepción de un pedido). */
public record EntradaComando(
        Long depositoId,
        String observacion,
        List<ItemEntradaComando> items,
        String referenciaTipo,
        Long referenciaId
) {
}

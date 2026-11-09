package com.tienda.backend.inventario.application.model;

/** Identifica una fila de stock: un lote en un depósito. */
public record ClaveStock(Long depositoId, Long loteId) {
}

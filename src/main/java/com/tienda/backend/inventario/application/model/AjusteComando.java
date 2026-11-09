package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;

/** Corrige el stock de un lote a lo que realmente hay ({@code stockReal}, en unidad base), por ejemplo tras un conteo físico. */
public record AjusteComando(Long depositoId, Long loteId, BigDecimal stockReal, String motivo) {
}

package com.tienda.backend.lote.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** R10: nota de ajuste del costo de un lote (valor anterior, nuevo, motivo y quién lo corrigió). */
public record AjusteCostoLote(Long id, Long loteId, BigDecimal costoAnterior, BigDecimal costoNuevo, String motivo,
                              String usuario, LocalDateTime fecha) {
}

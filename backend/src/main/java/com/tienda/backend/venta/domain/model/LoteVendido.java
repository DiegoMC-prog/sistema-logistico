package com.tienda.backend.venta.domain.model;

import java.math.BigDecimal;

/** De qué lote salió parte de una línea. La cantidad está en unidad base; el costo es el del lote al vender. */
public record LoteVendido(Long id, Long loteId, BigDecimal cantidadBase, BigDecimal costoUnitario) {
}

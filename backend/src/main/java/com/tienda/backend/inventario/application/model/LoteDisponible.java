package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Lote con stock en un depósito, candidato a una salida FEFO. */
public record LoteDisponible(
        Long loteId,
        Long productoId,
        LocalDate fechaVencimiento,
        BigDecimal costoUnitario,
        BigDecimal porcentajeDescuentoVencimiento
) {
}

package com.tienda.backend.lote.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos editables de un lote; el producto no se puede cambiar. */
public record EditarLoteComando(
        String numeroLote,
        LocalDate fechaVencimiento,
        BigDecimal costoUnitarioCompra,
        BigDecimal porcentajeDescuentoVencimiento
) {
}

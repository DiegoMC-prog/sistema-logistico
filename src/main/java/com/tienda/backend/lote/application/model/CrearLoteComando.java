package com.tienda.backend.lote.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos para crear un lote; {@code numeroLote} es opcional y, si falta, el sistema lo genera. */
public record CrearLoteComando(
        Long productoId,
        String numeroLote,
        LocalDate fechaVencimiento,
        BigDecimal costoUnitarioCompra,
        BigDecimal porcentajeDescuentoVencimiento
) {
}

package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos del lote que se crea al registrar una entrada. El número es opcional: si falta, se genera. */
public record NuevoLoteComando(
        String numeroLote,
        LocalDate fechaVencimiento,
        BigDecimal costoUnitarioCompra,
        BigDecimal porcentajeDescuentoVencimiento
) {
}

package com.tienda.backend.inventario.application.model;

import java.math.BigDecimal;

/** De qué lote salió una parte de una línea: {@code itemIndice} es la posición de la línea en el comando. */
public record AsignacionFefo(
        int itemIndice,
        Long loteId,
        BigDecimal cantidadBase,
        BigDecimal costoUnitario,
        BigDecimal porcentajeDescuentoVencimiento
) {
}

package com.tienda.backend.venta.application.model;

import java.math.BigDecimal;

/** De qué lote salió una parte de la línea {@code itemIndice}, con el costo y el descuento por vencimiento de ese lote. */
public record AsignacionStock(int itemIndice, Long loteId, BigDecimal cantidadBase, BigDecimal costoUnitario,
                              BigDecimal porcentajeDescuento) {
}

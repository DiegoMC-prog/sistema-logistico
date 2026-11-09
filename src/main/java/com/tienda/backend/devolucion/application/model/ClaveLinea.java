package com.tienda.backend.devolucion.application.model;

/** Una línea de venta y uno de los lotes de los que salió. */
public record ClaveLinea(Long ventaDetalleId, Long loteId) {
}

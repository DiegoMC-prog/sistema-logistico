package com.tienda.backend.devolucion.application.model;

import com.tienda.backend.devolucion.domain.model.Devolucion;

/** Devolución con el nombre de su método de pago y, si fue un cambio, un resumen de la venta nueva. */
public record DevolucionVista(Devolucion devolucion, String metodoPagoNombre, VentaCambioRef ventaCambio) {
}

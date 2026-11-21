package com.tienda.backend.abastecimiento.application.model;

/**
 * Valores globales del motor (app.reorden.* en properties). Los parámetros de costo y servicio se pueden
 * sobrescribir por producto.
 */
public record ConfigReorden(double costoPedido, double costoAlmacenPct, double nivelServicio, int diasVentana,
                            int historialMinimoDias, double leadTimeDefectoDias) {
}

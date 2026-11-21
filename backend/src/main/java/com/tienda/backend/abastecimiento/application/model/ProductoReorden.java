package com.tienda.backend.abastecimiento.application.model;

/** Producto a evaluar con sus parámetros propios (null = usar el valor global). */
public record ProductoReorden(Long productoId, String nombre, boolean perecedero, Double costoPedido,
                              Double costoAlmacenPct, Double nivelServicio, Long proveedorPreferidoId) {
}

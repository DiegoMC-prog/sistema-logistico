package com.tienda.backend.shared.application.model;

/** R08: operaciones de caja que exigen la autorización de un administrador cuando superan el monto umbral. */
public enum OperacionAutorizable {
    ANULACION_VENTA("anular una venta"),
    DEVOLUCION("registrar una devolución"),
    CAMBIO("registrar un cambio");

    private final String descripcion;

    OperacionAutorizable(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}

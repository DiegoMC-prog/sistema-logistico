package com.tienda.backend.precio.application.model;

import com.tienda.backend.precio.domain.model.Precio;

import java.math.BigDecimal;

/**
 * Precio junto con el nombre de su tipo de cliente (null si es el precio general) y, al resolver el precio de venta,
 * el recargo por escasez vigente (null si no hay).
 */
public record PrecioDetalle(Precio precio, String tipoClienteNombre, RecargoAplicado recargo) {

    public PrecioDetalle(Precio precio, String tipoClienteNombre) {
        this(precio, tipoClienteNombre, null);
    }

    public PrecioDetalle conRecargo(RecargoAplicado recargo) {
        return new PrecioDetalle(precio, tipoClienteNombre, recargo);
    }

    /** Precio a cobrar: el de la jerarquía de precios más el recargo por escasez, si hay. */
    public BigDecimal precioFinal() {
        return recargo == null ? precio.getPrecio() : recargo.aplicar(precio.getPrecio());
    }
}

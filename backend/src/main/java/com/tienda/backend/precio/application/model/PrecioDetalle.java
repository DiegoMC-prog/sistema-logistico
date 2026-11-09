package com.tienda.backend.precio.application.model;

import com.tienda.backend.precio.domain.model.Precio;

/** Precio junto con el nombre de su tipo de cliente (null si es el precio general). */
public record PrecioDetalle(Precio precio, String tipoClienteNombre) {
}

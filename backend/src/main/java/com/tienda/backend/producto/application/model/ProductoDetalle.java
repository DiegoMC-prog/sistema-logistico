package com.tienda.backend.producto.application.model;

import com.tienda.backend.producto.domain.model.Producto;

/** Modelo de lectura: el producto junto con el nombre de su categoría. */
public record ProductoDetalle(Producto producto, String categoriaNombre) {
}

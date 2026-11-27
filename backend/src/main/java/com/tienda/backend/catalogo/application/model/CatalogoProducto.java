package com.tienda.backend.catalogo.application.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Producto del catálogo con sus presentaciones.
 *
 * @param stock stock del depósito consultado, en unidades base (null si no se pidió un depósito)
 */
public record CatalogoProducto(Long productoId, String nombre, String categoria, BigDecimal stock, String unidadBase,
                               List<CatalogoPresentacion> presentaciones) {

    public CatalogoProducto conPresentaciones(List<CatalogoPresentacion> nuevas) {
        return new CatalogoProducto(productoId, nombre, categoria, stock, unidadBase, nuevas);
    }
}

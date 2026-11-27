package com.tienda.backend.catalogo.application.model;

import java.math.BigDecimal;

/**
 * Presentación de un producto.
 *
 * @param precio  precio de venta de una unidad, con el recargo incluido (null si no se pidió o no tiene precio)
 * @param recargo porcentaje de recargo por escasez incluido en el precio, o null
 */
public record CatalogoPresentacion(Long id, String abreviatura, BigDecimal factor, BigDecimal precio,
                                   BigDecimal recargo) {

    public CatalogoPresentacion conPrecio(PrecioCatalogo p) {
        return new CatalogoPresentacion(id, abreviatura, factor, p.precio(), p.recargo());
    }
}

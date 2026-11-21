package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;

/**
 * Indicador de precios de un proveedor: promedio simple de la variación a 90 días de los productos que se le compraron.
 *
 * @param productosConAlza productos cuya variación supera el umbral de alza del motor de reorden
 */
public record VariacionProveedor(
        Long proveedorId,
        String proveedor,
        int productos,
        int productosConAlza,
        BigDecimal variacionMedia30,
        BigDecimal variacionMedia90,
        BigDecimal mayorAlza90
) {
}

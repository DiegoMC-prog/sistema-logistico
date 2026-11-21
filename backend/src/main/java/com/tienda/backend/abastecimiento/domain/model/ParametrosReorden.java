package com.tienda.backend.abastecimiento.domain.model;

import com.tienda.backend.abastecimiento.domain.exception.ParametrosReordenInvalidosException;

/**
 * Parámetros de costo y servicio con que se evalúa un producto.
 *
 * @param costoPedido     S: costo de emitir un pedido
 * @param costoAlmacenPct costo anual de mantener una unidad, como fracción de su costo de compra (H = pct × costo)
 * @param nivelServicio   probabilidad de no quedarse sin stock durante el lead time
 */
public record ParametrosReorden(double costoPedido, double costoAlmacenPct, double nivelServicio) {

    public ParametrosReorden {
        if (costoPedido < 0) {
            throw new ParametrosReordenInvalidosException("El costo de emitir un pedido no puede ser negativo");
        }
        if (costoAlmacenPct <= 0) {
            throw new ParametrosReordenInvalidosException("El costo de almacenamiento debe ser mayor que cero");
        }
        if (nivelServicio < 0.5 || nivelServicio > 0.999) {
            throw new ParametrosReordenInvalidosException("El nivel de servicio debe estar entre 0.50 y 0.999");
        }
    }
}

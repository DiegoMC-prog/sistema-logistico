package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Evolución del costo de un producto con un proveedor. Las variaciones son fracciones (0.08 = +8 %) y valen null
 * cuando no hay compras suficientes en la ventana.
 *
 * @param alza true si la variación de la ventana del motor de reorden supera el umbral de alza
 */
public record VariacionCostoResumen(
        Long proveedorId,
        String proveedor,
        Long productoId,
        String producto,
        String categoria,
        int compras,
        LocalDate primeraCompra,
        LocalDate ultimaCompra,
        BigDecimal ultimoCosto,
        BigDecimal costoMinimo,
        BigDecimal costoMaximo,
        BigDecimal variacion30,
        BigDecimal variacion90,
        boolean alza
) {
}

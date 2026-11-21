package com.tienda.backend.abastecimiento.application.model;

import com.tienda.backend.abastecimiento.domain.model.AccionSugerencia;

import java.math.BigDecimal;

/** Lo necesario para convertir una sugerencia en una línea de pedido. */
public record SugerenciaParaPedido(Long id, Long productoId, String producto, Long proveedorId, AccionSugerencia accion,
                                   BigDecimal cantidadPesimista, BigDecimal cantidadEstandar,
                                   BigDecimal cantidadOptimista, BigDecimal costoUnitario) {
}

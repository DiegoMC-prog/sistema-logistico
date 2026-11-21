package com.tienda.backend.precio.application.model;

import com.tienda.backend.precio.domain.model.AlcanceRecargo;

import java.math.BigDecimal;

/**
 * Activar un recargo por escasez. Según el alcance se usa {@code productoId} o {@code categoriaId}.
 *
 * @param porcentaje fracción sobre el precio (0.15 = +15 %), mayor que 0 y como mucho 1
 */
public record CrearRecargoComando(AlcanceRecargo alcance, Long productoId, Long categoriaId, BigDecimal porcentaje,
                                  String motivo) {
}

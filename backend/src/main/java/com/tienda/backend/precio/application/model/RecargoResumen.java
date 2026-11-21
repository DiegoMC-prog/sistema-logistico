package com.tienda.backend.precio.application.model;

import com.tienda.backend.precio.domain.model.AlcanceRecargo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Recargo por escasez con su destino y su auditoría (quién lo activó y quién lo desactivó). */
public record RecargoResumen(
        Long id,
        AlcanceRecargo alcance,
        Long productoId,
        String producto,
        Long categoriaId,
        String categoria,
        /** Fracción: 0.15 = +15 %. */
        BigDecimal porcentaje,
        String motivo,
        boolean activo,
        String activadoPor,
        LocalDateTime activadoAt,
        String desactivadoPor,
        LocalDateTime desactivadoAt
) {
    /** Nombre del producto o de la categoría, según el alcance. */
    public String destino() {
        return alcance == AlcanceRecargo.PRODUCTO ? producto : categoria;
    }
}

package com.tienda.backend.precio.application.model;

import com.tienda.backend.precio.domain.model.AlcanceRecargo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Recargo por escasez vigente para una presentación.
 *
 * @param porcentaje fracción sobre el precio resuelto (0.15 = +15 %)
 */
public record RecargoAplicado(Long id, AlcanceRecargo alcance, BigDecimal porcentaje, String motivo) {

    /** Precio con el recargo, redondeado a centavos. */
    public BigDecimal aplicar(BigDecimal precio) {
        return precio.multiply(BigDecimal.ONE.add(porcentaje)).setScale(2, RoundingMode.HALF_UP);
    }
}

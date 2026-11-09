package com.tienda.backend.reporte.application.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Resultado de un reporte: el rango aplicado, los totales generales y el detalle. Los totales son un mapa
 * (nombre a valor) porque cada reporte tiene los suyos; el detalle es una lista de registros del mismo tipo.
 */
public record ReporteResultado<F>(
        LocalDate desde,
        LocalDate hasta,
        Map<String, Object> totales,
        List<F> filas
) {
}

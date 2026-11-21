package com.tienda.backend.abastecimiento.application.model;

import java.time.LocalDateTime;
import java.util.Map;

/** Un recálculo de sugerencias y cuántos productos quedaron en cada estado. */
public record CalculoResumen(Long id, LocalDateTime fecha, OrigenCalculo origen, String ejecutadoPor, int diasVentana,
                             int historialMinimoDias, int productosEvaluados, int productosAReordenar,
                             Map<String, Long> porEstado) {
}

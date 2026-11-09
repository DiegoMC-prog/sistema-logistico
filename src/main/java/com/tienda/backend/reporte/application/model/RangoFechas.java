package com.tienda.backend.reporte.application.model;

import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Rango de fechas de un reporte, con ambos extremos incluidos. Sin datos: los últimos 30 días hasta hoy. */
public record RangoFechas(LocalDate desde, LocalDate hasta) {
    public static final int DIAS_DEFAULT = 30;
    public static final int DIAS_MAXIMO = 366;

    public static RangoFechas de(LocalDate desde, LocalDate hasta) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(DIAS_DEFAULT - 1);
        if (inicio.isAfter(fin)) {
            throw new ReporteParametrosInvalidosException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        if (inicio.plusDays(DIAS_MAXIMO).isBefore(fin)) {
            throw new ReporteParametrosInvalidosException(
                    "El rango del reporte no puede superar " + DIAS_MAXIMO + " días");
        }
        return new RangoFechas(inicio, fin);
    }

    /** Primer instante del rango (inclusive). */
    public LocalDateTime inicio() {
        return desde.atStartOfDay();
    }

    /** Primer instante posterior al rango (exclusivo): así incluye todo el último día. */
    public LocalDateTime finExclusivo() {
        return hasta.plusDays(1).atStartOfDay();
    }
}

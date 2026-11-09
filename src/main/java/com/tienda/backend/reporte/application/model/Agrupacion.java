package com.tienda.backend.reporte.application.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/** Cómo se agrupan los reportes por período. La semana empieza el lunes. */
public enum Agrupacion {
    DIA("day"),
    SEMANA("week"),
    MES("month");

    private final String unidadSql;

    Agrupacion(String unidadSql) {
        this.unidadSql = unidadSql;
    }

    /** Valor fijo (nunca viene del cliente) para {@code date_trunc}. */
    public String unidadSql() {
        return unidadSql;
    }

    /** Primer día del período al que pertenece la fecha. */
    public LocalDate inicioDe(LocalDate fecha) {
        return switch (this) {
            case DIA -> fecha;
            case SEMANA -> fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MES -> fecha.withDayOfMonth(1);
        };
    }

    public LocalDate siguiente(LocalDate inicioPeriodo) {
        return switch (this) {
            case DIA -> inicioPeriodo.plusDays(1);
            case SEMANA -> inicioPeriodo.plusWeeks(1);
            case MES -> inicioPeriodo.plusMonths(1);
        };
    }
}

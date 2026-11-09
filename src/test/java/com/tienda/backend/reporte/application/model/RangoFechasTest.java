package com.tienda.backend.reporte.application.model;

import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RangoFechasTest {

    @Test
    void sinDatosSonLosUltimos30DiasHastaHoy() {
        RangoFechas r = RangoFechas.de(null, null);
        assertEquals(LocalDate.now(), r.hasta());
        assertEquals(LocalDate.now().minusDays(29), r.desde());
    }

    @Test
    void soloDesdeLlegaHastaHoy() {
        RangoFechas r = RangoFechas.de(LocalDate.of(2026, 1, 1), null);
        assertEquals(LocalDate.now(), r.hasta());
    }

    @Test
    void elUltimoDiaQuedaIncluidoPorqueElFinEsExclusivo() {
        RangoFechas r = RangoFechas.de(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));
        assertEquals(LocalDateTime.of(2026, 3, 1, 0, 0), r.inicio());
        assertEquals(LocalDateTime.of(2026, 4, 1, 0, 0), r.finExclusivo());
    }

    @Test
    void rechazaDesdePosteriorAHasta() {
        assertThrows(ReporteParametrosInvalidosException.class,
                () -> RangoFechas.de(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 1)));
    }

    @Test
    void rechazaRangosMayoresAUnAnio() {
        assertThrows(ReporteParametrosInvalidosException.class,
                () -> RangoFechas.de(LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1)));
        RangoFechas.de(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));   // un año exacto sí
    }

    @Test
    void agrupaPorSemanaDesdeElLunesYPorMesDesdeElPrimero() {
        LocalDate miercoles = LocalDate.of(2026, 11, 4);
        assertEquals(LocalDate.of(2026, 11, 2), Agrupacion.SEMANA.inicioDe(miercoles));
        assertEquals(LocalDate.of(2026, 11, 1), Agrupacion.MES.inicioDe(miercoles));
        assertEquals(miercoles, Agrupacion.DIA.inicioDe(miercoles));
        assertEquals(LocalDate.of(2026, 11, 9), Agrupacion.SEMANA.siguiente(LocalDate.of(2026, 11, 2)));
        assertEquals(LocalDate.of(2026, 12, 1), Agrupacion.MES.siguiente(LocalDate.of(2026, 11, 1)));
    }
}

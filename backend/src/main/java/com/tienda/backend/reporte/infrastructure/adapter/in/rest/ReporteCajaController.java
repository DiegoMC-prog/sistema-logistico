package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import com.tienda.backend.reporte.application.model.AgrupacionDiferencias;
import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteCajaUseCase;
import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Reportes de caja. Aceptan {@code desde} y {@code hasta} (yyyy-MM-dd; por defecto los últimos 30 días, según la
 * fecha de apertura del turno) y {@code formato=json|csv}.
 */
@RestController
@RequestMapping("/api/v1/reportes/caja")
@RequiredArgsConstructor
public class ReporteCajaController {

    private final ConsultarReporteCajaUseCase consultarReporteCajaUseCase;

    @GetMapping("/cierres")
    public ResponseEntity<?> cierres(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long cajaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String formato) {
        return RespuestaReporte.de(consultarReporteCajaUseCase.cierres(RangoFechas.de(desde, hasta), cajaId, usuarioId, estado),
                formato, "cierres-de-caja", FilasCaja.Cierre.class);
    }

    /** {@code agrupar=TURNO} (por defecto: solo los turnos con diferencia), {@code CAJERO} o {@code CAJA}. */
    @GetMapping("/diferencias")
    public ResponseEntity<?> diferencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) Long cajaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) String formato) {
        AgrupacionDiferencias agrupacion = AgrupacionDiferencias.TURNO;
        if (agrupar != null && !agrupar.isBlank()) {
            try {
                agrupacion = AgrupacionDiferencias.valueOf(agrupar.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ReporteParametrosInvalidosException("El parámetro 'agrupar' debe ser TURNO, CAJERO o CAJA");
            }
        }
        RangoFechas rango = RangoFechas.de(desde, hasta);
        if (agrupacion == AgrupacionDiferencias.TURNO) {
            return RespuestaReporte.de(consultarReporteCajaUseCase.diferenciasPorTurno(rango, cajaId, usuarioId), formato,
                    "diferencias-de-caja", FilasCaja.Cierre.class);
        }
        return RespuestaReporte.de(consultarReporteCajaUseCase.diferenciasResumen(rango, agrupacion == AgrupacionDiferencias.CAJERO,
                cajaId, usuarioId), formato, "diferencias-por-" + agrupacion.name().toLowerCase(Locale.ROOT),
                FilasCaja.Diferencias.class);
    }
}

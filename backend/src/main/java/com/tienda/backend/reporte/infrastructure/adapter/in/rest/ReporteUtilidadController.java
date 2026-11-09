package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteUtilidadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Reportes de utilidad. Revelan costos y márgenes, por eso tienen su propio permiso. Aceptan {@code desde} y
 * {@code hasta} (yyyy-MM-dd; por defecto los últimos 30 días) y {@code formato=json|csv}.
 */
@RestController
@RequestMapping("/api/v1/reportes/utilidad")
@RequiredArgsConstructor
public class ReporteUtilidadController {

    private final ConsultarReporteUtilidadUseCase consultarReporteUtilidadUseCase;

    @GetMapping("/por-producto")
    public ResponseEntity<?> porProducto(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String formato) {
        return RespuestaReporte.de(consultarReporteUtilidadUseCase.porProducto(RangoFechas.de(desde, hasta), categoriaId, limite),
                formato, "utilidad-por-producto", FilasUtilidad.Producto.class);
    }

    @GetMapping("/por-periodo")
    public ResponseEntity<?> porPeriodo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Agrupacion agrupacion,
            @RequestParam(required = false) String formato) {
        return RespuestaReporte.de(consultarReporteUtilidadUseCase.porPeriodo(RangoFechas.de(desde, hasta), agrupacion),
                formato, "utilidad-por-periodo", FilasUtilidad.Periodo.class);
    }

    @GetMapping("/precios-manuales")
    public ResponseEntity<?> preciosManuales(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String formato) {
        return RespuestaReporte.de(consultarReporteUtilidadUseCase.preciosManuales(RangoFechas.de(desde, hasta), limite),
                formato, "precios-manuales", FilasUtilidad.PrecioManual.class);
    }
}

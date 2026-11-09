package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import com.tienda.backend.reporte.application.model.AgrupacionStock;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteInventarioUseCase;
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
 * Reportes de inventario. Stock valorizado, stock bajo y lotes por vencer muestran el estado actual (sin rango de
 * fechas); el kardex sí lleva {@code desde} y {@code hasta}. Todos aceptan {@code formato=json|csv}.
 */
@RestController
@RequestMapping("/api/v1/reportes/inventario")
@RequiredArgsConstructor
public class ReporteInventarioController {

    private final ConsultarReporteInventarioUseCase consultarReporteInventarioUseCase;

    /** {@code agrupar=PRODUCTO} (por defecto), {@code DEPOSITO} o {@code CATEGORIA}. */
    @GetMapping("/stock-valorizado")
    public ResponseEntity<?> stockValorizado(
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String formato) {
        AgrupacionStock agrupacion = AgrupacionStock.PRODUCTO;
        if (agrupar != null && !agrupar.isBlank()) {
            try {
                agrupacion = AgrupacionStock.valueOf(agrupar.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new ReporteParametrosInvalidosException("El parámetro 'agrupar' debe ser PRODUCTO, DEPOSITO o CATEGORIA");
            }
        }
        return responder(consultarReporteInventarioUseCase.stockValorizado(agrupacion, depositoId, categoriaId),
                formato, "stock-valorizado", FilasInventario.Stock.class);
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<?> stockBajo(
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String formato) {
        return responder(consultarReporteInventarioUseCase.stockBajo(depositoId, categoriaId), formato,
                "stock-bajo", FilasInventario.StockBajo.class);
    }

    /** {@code dias}: ventana hacia adelante (por defecto 30). {@code incluirVencidos} por defecto true. */
    @GetMapping("/lotes-por-vencer")
    public ResponseEntity<?> lotesPorVencer(
            @RequestParam(required = false) Integer dias,
            @RequestParam(defaultValue = "true") boolean incluirVencidos,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) String formato) {
        return responder(consultarReporteInventarioUseCase.lotesPorVencer(dias, incluirVencidos, depositoId), formato,
                "lotes-por-vencer", FilasInventario.LoteVencimiento.class);
    }

    @GetMapping("/kardex")
    public ResponseEntity<?> kardex(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String formato) {
        return responder(consultarReporteInventarioUseCase.kardex(productoId, depositoId, RangoFechas.de(desde, hasta)),
                formato, "kardex-producto-" + productoId, FilasInventario.Kardex.class);
    }

    private static <F extends Record> ResponseEntity<?> responder(ReporteResultado<F> resultado, String formato,
                                                                   String nombreArchivo, Class<F> tipo) {
        String f = formato == null ? "json" : formato.trim().toLowerCase(Locale.ROOT);
        return switch (f) {
            case "json", "" -> ResponseEntity.ok(resultado);
            case "csv" -> CsvSupport.respuesta(nombreArchivo + (resultado.desde() == null ? "-" + LocalDate.now()
                    : "-" + resultado.desde() + "_" + resultado.hasta()), resultado.filas(), tipo);
            default -> throw new ReporteParametrosInvalidosException("El formato debe ser json o csv");
        };
    }
}

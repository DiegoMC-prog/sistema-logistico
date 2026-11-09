package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasVentas;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteVentasUseCase;
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
 * Reportes de ventas. Todos aceptan {@code desde} y {@code hasta} (yyyy-MM-dd; por defecto los últimos 30 días) y
 * {@code formato=json|csv}. En CSV se exporta solo el detalle ({@code filas}).
 */
@RestController
@RequestMapping("/api/v1/reportes/ventas")
@RequiredArgsConstructor
public class ReporteVentasController {

    private final ConsultarReporteVentasUseCase consultarReporteVentasUseCase;

    @GetMapping("/resumen")
    public ResponseEntity<?> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Agrupacion agrupacion,
            @RequestParam(required = false) String formato) {
        ReporteResultado<FilasVentas.Periodo> r = consultarReporteVentasUseCase.resumen(RangoFechas.de(desde, hasta), agrupacion);
        return responder(r, formato, "ventas-resumen", FilasVentas.Periodo.class);
    }

    @GetMapping("/por-producto")
    public ResponseEntity<?> porProducto(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String formato) {
        ReporteResultado<FilasVentas.Producto> r = consultarReporteVentasUseCase.porProducto(
                RangoFechas.de(desde, hasta), categoriaId, limite);
        return responder(r, formato, "ventas-por-producto", FilasVentas.Producto.class);
    }

    @GetMapping("/productos-sin-ventas")
    public ResponseEntity<?> productosSinVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String formato) {
        ReporteResultado<FilasVentas.ProductoSinVentas> r = consultarReporteVentasUseCase.productosSinVentas(
                RangoFechas.de(desde, hasta), categoriaId, limite);
        return responder(r, formato, "productos-sin-ventas", FilasVentas.ProductoSinVentas.class);
    }

    @GetMapping("/por-cajero")
    public ResponseEntity<?> porCajero(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String formato) {
        ReporteResultado<FilasVentas.Origen> r = consultarReporteVentasUseCase.porCajero(RangoFechas.de(desde, hasta));
        return responder(r, formato, "ventas-por-cajero", FilasVentas.Origen.class);
    }

    @GetMapping("/por-caja")
    public ResponseEntity<?> porCaja(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String formato) {
        ReporteResultado<FilasVentas.Origen> r = consultarReporteVentasUseCase.porCaja(RangoFechas.de(desde, hasta));
        return responder(r, formato, "ventas-por-caja", FilasVentas.Origen.class);
    }

    @GetMapping("/por-metodo-pago")
    public ResponseEntity<?> porMetodoPago(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String formato) {
        ReporteResultado<FilasVentas.MetodoPago> r = consultarReporteVentasUseCase.porMetodoPago(RangoFechas.de(desde, hasta));
        return responder(r, formato, "ventas-por-metodo-pago", FilasVentas.MetodoPago.class);
    }

    /** {@code agrupar=CLIENTE} (por defecto) o {@code TIPO}. */
    @GetMapping("/por-cliente")
    public ResponseEntity<?> porCliente(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String agrupar,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String formato) {
        boolean porTipo = false;
        if (agrupar != null && !agrupar.isBlank()) {
            String a = agrupar.trim().toUpperCase(Locale.ROOT);
            if (!a.equals("CLIENTE") && !a.equals("TIPO")) {
                throw new ReporteParametrosInvalidosException("El parámetro 'agrupar' debe ser CLIENTE o TIPO");
            }
            porTipo = a.equals("TIPO");
        }
        ReporteResultado<FilasVentas.Cliente> r = consultarReporteVentasUseCase.porCliente(
                RangoFechas.de(desde, hasta), porTipo, limite);
        return responder(r, formato, "ventas-por-cliente", FilasVentas.Cliente.class);
    }

    @GetMapping("/anulaciones-devoluciones")
    public ResponseEntity<?> incidencias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String formato) {
        ReporteResultado<FilasVentas.Incidencia> r = consultarReporteVentasUseCase.incidencias(
                RangoFechas.de(desde, hasta), limite);
        return responder(r, formato, "anulaciones-devoluciones", FilasVentas.Incidencia.class);
    }

    private static <F extends Record> ResponseEntity<?> responder(ReporteResultado<F> resultado, String formato,
                                                                   String nombreArchivo, Class<F> tipo) {
        String f = formato == null ? "json" : formato.trim().toLowerCase(Locale.ROOT);
        return switch (f) {
            case "json", "" -> ResponseEntity.ok(resultado);
            case "csv" -> CsvSupport.respuesta(nombreArchivo + "-" + resultado.desde() + "_" + resultado.hasta(),
                    resultado.filas(), tipo);
            default -> throw new ReporteParametrosInvalidosException("El formato debe ser json o csv");
        };
    }
}

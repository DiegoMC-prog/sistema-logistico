package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import com.tienda.backend.reporte.application.model.ReporteResultado;
import com.tienda.backend.reporte.domain.exception.ReporteParametrosInvalidosException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Locale;

/** Arma la respuesta de un reporte en el formato pedido: JSON (todo el resultado) o CSV (solo el detalle). */
final class RespuestaReporte {
    private RespuestaReporte() {
    }

    static <F extends Record> ResponseEntity<?> de(ReporteResultado<F> resultado, String formato, String nombreArchivo,
                                                    Class<F> tipo) {
        String f = formato == null ? "json" : formato.trim().toLowerCase(Locale.ROOT);
        return switch (f) {
            case "json", "" -> ResponseEntity.ok(resultado);
            case "csv" -> CsvSupport.respuesta(nombreArchivo + sufijoDeFechas(resultado), resultado.filas(), tipo);
            default -> throw new ReporteParametrosInvalidosException("El formato debe ser json o csv");
        };
    }

    /** Los reportes del estado actual no tienen rango: el archivo lleva la fecha de hoy. */
    private static String sufijoDeFechas(ReporteResultado<?> r) {
        return r.desde() == null ? "-" + LocalDate.now() : "-" + r.desde() + "_" + r.hasta();
    }
}

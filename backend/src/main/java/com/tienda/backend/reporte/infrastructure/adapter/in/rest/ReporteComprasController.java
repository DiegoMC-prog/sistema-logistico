package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.in.ConsultarReporteComprasUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Reportes de compras. {@code por-proveedor} y {@code evolucion-costo} aceptan {@code desde} y {@code hasta}
 * (yyyy-MM-dd; por defecto los últimos 30 días); {@code pendientes} muestra el estado actual. Todos aceptan
 * {@code formato=json|csv}.
 */
@RestController
@RequestMapping("/api/v1/reportes/compras")
@RequiredArgsConstructor
public class ReporteComprasController {

    private final ConsultarReporteComprasUseCase consultarReporteComprasUseCase;

    /** Rango según la fecha del pedido. */
    @GetMapping("/por-proveedor")
    public ResponseEntity<?> porProveedor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String formato) {
        return RespuestaReporte.de(consultarReporteComprasUseCase.porProveedor(RangoFechas.de(desde, hasta)), formato,
                "compras-por-proveedor", FilasCompras.Proveedor.class);
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> pendientes(
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) String formato) {
        return RespuestaReporte.de(consultarReporteComprasUseCase.pendientes(proveedorId, depositoId), formato,
                "pedidos-pendientes", FilasCompras.Pendiente.class);
    }

    /** {@code productoId} es obligatorio; el rango se aplica a la fecha de recepción. */
    @GetMapping("/evolucion-costo")
    public ResponseEntity<?> evolucionCosto(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String formato) {
        return RespuestaReporte.de(consultarReporteComprasUseCase.evolucionCosto(productoId, proveedorId,
                RangoFechas.de(desde, hasta)), formato, "evolucion-costo-producto-" + productoId,
                FilasCompras.CostoProducto.class);
    }
}

package com.tienda.backend.devolucion.infrastructure.adapter.in.rest;

import com.tienda.backend.devolucion.application.model.DevolucionFiltro;
import com.tienda.backend.devolucion.application.model.ResultadoRegistroDevolucion;
import com.tienda.backend.devolucion.application.port.in.ConsultarDevolucionUseCase;
import com.tienda.backend.devolucion.application.port.in.RegistrarCambioUseCase;
import com.tienda.backend.devolucion.application.port.in.RegistrarDevolucionUseCase;
import com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto.CambioRequest;
import com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto.CambioResponse;
import com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto.DevolucionRequest;
import com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto.DevolucionResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DevolucionController {

    private final RegistrarDevolucionUseCase registrarDevolucionUseCase;
    private final RegistrarCambioUseCase registrarCambioUseCase;
    private final ConsultarDevolucionUseCase consultarDevolucionUseCase;

    /**
     * Cabecera {@code Idempotency-Key} opcional: si el POS reintenta con la misma clave y el mismo contenido, devuelve
     * la devolución ya registrada con 200 en vez de reembolsar dos veces.
     */
    @PostMapping("/ventas/{ventaId}/devoluciones")
    public ResponseEntity<DevolucionResponse> registrar(
            @PathVariable Long ventaId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody DevolucionRequest request) {
        ResultadoRegistroDevolucion r = registrarDevolucionUseCase.registrar(request.toComando(ventaId, idempotencyKey));
        return ResponseEntity.status(r.creada() ? HttpStatus.CREATED : HttpStatus.OK).body(DevolucionResponse.from(r.vista()));
    }

    /** Cambio por otro producto: devuelve lo indicado y crea la venta nueva en una sola operación. */
    @PostMapping("/ventas/{ventaId}/cambios")
    public ResponseEntity<CambioResponse> registrarCambio(
            @PathVariable Long ventaId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CambioRequest request) {
        ResultadoRegistroDevolucion r = registrarCambioUseCase.registrarCambio(request.toComando(ventaId, idempotencyKey));
        return ResponseEntity.status(r.creada() ? HttpStatus.CREATED : HttpStatus.OK).body(CambioResponse.from(r.vista()));
    }

    @GetMapping("/ventas/{ventaId}/devoluciones")
    public ResponseEntity<PageResult<DevolucionResponse>> deLaVenta(@PathVariable Long ventaId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "20") int size) {
        DevolucionFiltro filtro = new DevolucionFiltro(ventaId, null, null, null, null);
        return ResponseEntity.ok(consultarDevolucionUseCase.listar(filtro, page, size).map(DevolucionResponse::resumen));
    }

    @GetMapping("/devoluciones")
    public ResponseEntity<PageResult<DevolucionResponse>> listar(
            @RequestParam(required = false) Long ventaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long turnoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        DevolucionFiltro filtro = new DevolucionFiltro(ventaId, usuarioId, turnoId, desde, hasta);
        return ResponseEntity.ok(consultarDevolucionUseCase.listar(filtro, page, size).map(DevolucionResponse::resumen));
    }

    @GetMapping("/devoluciones/{id}")
    public ResponseEntity<DevolucionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(DevolucionResponse.from(consultarDevolucionUseCase.obtener(id)));
    }
}

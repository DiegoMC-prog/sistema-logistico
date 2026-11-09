package com.tienda.backend.venta.infrastructure.adapter.in.rest;

import com.tienda.backend.venta.application.model.ResultadoRegistroVenta;
import com.tienda.backend.venta.application.model.VentaFiltro;
import com.tienda.backend.venta.application.port.in.AnularVentaUseCase;
import com.tienda.backend.venta.application.port.in.ConsultarVentaUseCase;
import com.tienda.backend.venta.application.port.in.RegistrarVentaUseCase;
import com.tienda.backend.venta.domain.model.EstadoVenta;
import com.tienda.backend.venta.infrastructure.adapter.in.rest.dto.AnularVentaRequest;
import com.tienda.backend.venta.infrastructure.adapter.in.rest.dto.VentaRequest;
import com.tienda.backend.venta.infrastructure.adapter.in.rest.dto.VentaResponse;
import com.tienda.backend.venta.infrastructure.adapter.in.rest.dto.VentaResumenResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final RegistrarVentaUseCase registrarVentaUseCase;
    private final AnularVentaUseCase anularVentaUseCase;
    private final ConsultarVentaUseCase consultarVentaUseCase;

    /**
     * Cabecera {@code Idempotency-Key} opcional: si el POS reintenta con la misma clave y el mismo contenido (por un
     * corte de red, por ejemplo), devuelve la venta ya creada con 200 en vez de duplicarla.
     */
    @PostMapping
    public ResponseEntity<VentaResponse> registrar(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody VentaRequest request) {
        ResultadoRegistroVenta resultado = registrarVentaUseCase.registrar(request.toComando(idempotencyKey));
        return ResponseEntity.status(resultado.creada() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(VentaResponse.from(resultado.vista()));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<VentaResponse> anular(@PathVariable Long id, @Valid @RequestBody AnularVentaRequest request) {
        return ResponseEntity.ok(VentaResponse.from(anularVentaUseCase.anular(id, request.motivo())));
    }

    @GetMapping
    public ResponseEntity<PageResult<VentaResumenResponse>> listar(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) Long turnoId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        VentaFiltro filtro = new VentaFiltro(numero, turnoId, clienteId, usuarioId, estado, desde, hasta);
        return ResponseEntity.ok(consultarVentaUseCase.listar(filtro, page, size).map(VentaResumenResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(VentaResponse.from(consultarVentaUseCase.obtener(id)));
    }
}

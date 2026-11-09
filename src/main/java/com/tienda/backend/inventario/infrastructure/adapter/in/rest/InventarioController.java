package com.tienda.backend.inventario.infrastructure.adapter.in.rest;

import com.tienda.backend.inventario.application.model.MovimientoFiltro;
import com.tienda.backend.inventario.application.model.StockFiltro;
import com.tienda.backend.inventario.application.port.in.ConsultarStockUseCase;
import com.tienda.backend.inventario.application.port.in.ListarMovimientosUseCase;
import com.tienda.backend.inventario.application.port.in.ObtenerMovimientoUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarAjusteUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarEntradaUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarSalidaUseCase;
import com.tienda.backend.inventario.application.port.in.RegistrarTrasladoUseCase;
import com.tienda.backend.inventario.domain.model.TipoMovimiento;
import com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto.AjusteRequest;
import com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto.EntradaRequest;
import com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto.MovimientoResponse;
import com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto.SalidaRequest;
import com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto.StockResponse;
import com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto.TrasladoRequest;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final RegistrarEntradaUseCase registrarEntradaUseCase;
    private final RegistrarSalidaUseCase registrarSalidaUseCase;
    private final RegistrarTrasladoUseCase registrarTrasladoUseCase;
    private final RegistrarAjusteUseCase registrarAjusteUseCase;
    private final ConsultarStockUseCase consultarStockUseCase;
    private final ListarMovimientosUseCase listarMovimientosUseCase;
    private final ObtenerMovimientoUseCase obtenerMovimientoUseCase;

    @PostMapping("/entradas")
    public ResponseEntity<MovimientoResponse> entrada(@Valid @RequestBody EntradaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimientoResponse.from(registrarEntradaUseCase.registrarEntrada(request.toComando())));
    }

    @PostMapping("/salidas")
    public ResponseEntity<MovimientoResponse> salida(@Valid @RequestBody SalidaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimientoResponse.from(registrarSalidaUseCase.registrarSalida(request.toComando())));
    }

    @PostMapping("/traslados")
    public ResponseEntity<MovimientoResponse> traslado(@Valid @RequestBody TrasladoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimientoResponse.from(registrarTrasladoUseCase.registrarTraslado(request.toComando())));
    }

    @PostMapping("/ajustes")
    public ResponseEntity<MovimientoResponse> ajuste(@Valid @RequestBody AjusteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MovimientoResponse.from(registrarAjusteUseCase.registrarAjuste(request.toComando())));
    }

    @GetMapping
    public ResponseEntity<PageResult<StockResponse>> stock(
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long loteId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean conStock,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate venceHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        StockFiltro filtro = new StockFiltro(depositoId, productoId, loteId, search, conStock, venceHasta);
        return ResponseEntity.ok(consultarStockUseCase.consultarStock(filtro, page, size).map(StockResponse::from));
    }

    @GetMapping("/movimientos")
    public ResponseEntity<PageResult<MovimientoResponse>> movimientos(
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) Long loteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        MovimientoFiltro filtro = new MovimientoFiltro(tipo, depositoId, productoId, loteId, desde, hasta);
        return ResponseEntity.ok(listarMovimientosUseCase.listar(filtro, page, size).map(MovimientoResponse::from));
    }

    @GetMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoResponse> movimiento(@PathVariable Long id) {
        return ResponseEntity.ok(MovimientoResponse.from(obtenerMovimientoUseCase.obtener(id)));
    }
}

package com.tienda.backend.caja.infrastructure.adapter.in.rest;

import com.tienda.backend.caja.application.model.TurnoFiltro;
import com.tienda.backend.caja.application.port.in.CerrarTurnoUseCase;
import com.tienda.backend.caja.application.port.in.ConsultarTurnoUseCase;
import com.tienda.backend.caja.application.port.in.RegistrarMovimientoCajaUseCase;
import com.tienda.backend.caja.domain.model.EstadoTurno;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.CerrarTurnoRequest;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.MovimientoCajaRequest;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.MovimientoCajaResponse;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.TurnoResponse;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.TurnoResumenResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final ConsultarTurnoUseCase consultarTurnoUseCase;
    private final CerrarTurnoUseCase cerrarTurnoUseCase;
    private final RegistrarMovimientoCajaUseCase registrarMovimientoCajaUseCase;

    @GetMapping
    public ResponseEntity<PageResult<TurnoResponse>> listar(
            @RequestParam(required = false) Long cajaId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) EstadoTurno estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        TurnoFiltro filtro = new TurnoFiltro(cajaId, usuarioId, estado, desde, hasta);
        return ResponseEntity.ok(consultarTurnoUseCase.listar(filtro, page, size).map(TurnoResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResumenResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(TurnoResumenResponse.from(consultarTurnoUseCase.obtener(id)));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<TurnoResumenResponse> cerrar(@PathVariable Long id, @Valid @RequestBody CerrarTurnoRequest request) {
        return ResponseEntity.ok(TurnoResumenResponse.from(cerrarTurnoUseCase.cerrar(id, request.montoDeclarado())));
    }

    @PostMapping("/{id}/movimientos")
    public ResponseEntity<MovimientoCajaResponse> registrarMovimiento(@PathVariable Long id,
                                                                      @Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(MovimientoCajaResponse.from(
                registrarMovimientoCajaUseCase.registrar(id, request.tipo(), request.monto(), request.motivo())));
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<PageResult<MovimientoCajaResponse>> movimientos(@PathVariable Long id,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultarTurnoUseCase.movimientos(id, page, size).map(MovimientoCajaResponse::from));
    }
}

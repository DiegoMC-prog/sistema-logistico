package com.tienda.backend.caja.infrastructure.adapter.in.rest;

import com.tienda.backend.caja.application.model.CajaFiltro;
import com.tienda.backend.caja.application.port.in.AbrirTurnoUseCase;
import com.tienda.backend.caja.application.port.in.CambiarEstadoCajaUseCase;
import com.tienda.backend.caja.application.port.in.ConsultarTurnoUseCase;
import com.tienda.backend.caja.application.port.in.CrearCajaUseCase;
import com.tienda.backend.caja.application.port.in.EditarCajaUseCase;
import com.tienda.backend.caja.application.port.in.EliminarCajaUseCase;
import com.tienda.backend.caja.application.port.in.ListarCajaUseCase;
import com.tienda.backend.caja.application.port.in.ObtenerCajaUseCase;
import com.tienda.backend.caja.domain.model.EstadoCaja;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.AbrirTurnoRequest;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.CajaRequest;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.CajaResponse;
import com.tienda.backend.caja.infrastructure.adapter.in.rest.dto.TurnoResumenResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cajas")
@RequiredArgsConstructor
public class CajaController {

    private final CrearCajaUseCase crearCajaUseCase;
    private final EditarCajaUseCase editarCajaUseCase;
    private final ListarCajaUseCase listarCajaUseCase;
    private final ObtenerCajaUseCase obtenerCajaUseCase;
    private final EliminarCajaUseCase eliminarCajaUseCase;
    private final CambiarEstadoCajaUseCase cambiarEstadoCajaUseCase;
    private final AbrirTurnoUseCase abrirTurnoUseCase;
    private final ConsultarTurnoUseCase consultarTurnoUseCase;

    @PostMapping
    public ResponseEntity<CajaResponse> crear(@Valid @RequestBody CajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CajaResponse.from(crearCajaUseCase.create(request.toDomain())));
    }

    @GetMapping
    public ResponseEntity<PageResult<CajaResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) EstadoCaja estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        CajaFiltro filtro = new CajaFiltro(search, depositoId, estado);
        return ResponseEntity.ok(listarCajaUseCase.listar(filtro, page, size).map(CajaResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(CajaResponse.from(obtenerCajaUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CajaResponse> editar(@PathVariable Long id, @Valid @RequestBody CajaRequest request) {
        return ResponseEntity.ok(CajaResponse.from(editarCajaUseCase.editar(id, request.toDomain())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<CajaResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(CajaResponse.from(cambiarEstadoCajaUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<CajaResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(CajaResponse.from(cambiarEstadoCajaUseCase.desactivar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarCajaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/turnos")
    public ResponseEntity<TurnoResumenResponse> abrirTurno(@PathVariable Long id,
                                                           @Valid @RequestBody AbrirTurnoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TurnoResumenResponse.from(abrirTurnoUseCase.abrir(id, request.montoApertura())));
    }

    @GetMapping("/{id}/turno-actual")
    public ResponseEntity<TurnoResumenResponse> turnoActual(@PathVariable Long id) {
        return ResponseEntity.ok(TurnoResumenResponse.from(consultarTurnoUseCase.turnoActual(id)));
    }
}

package com.tienda.backend.metodopago.infrastructure.adapter.in.rest;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.metodopago.application.model.MetodoPagoFiltro;
import com.tienda.backend.metodopago.application.port.in.CambiarEstadoMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.CrearMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.EditarMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.EliminarMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.ListarMetodoPagoUseCase;
import com.tienda.backend.metodopago.application.port.in.ObtenerMetodoPagoUseCase;
import com.tienda.backend.metodopago.domain.model.EstadoMetodoPago;
import com.tienda.backend.metodopago.infrastructure.adapter.in.rest.dto.MetodoPagoRequest;
import com.tienda.backend.metodopago.infrastructure.adapter.in.rest.dto.MetodoPagoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/metodos-pago")
@RequiredArgsConstructor
public class MetodoPagoController {

    private final CrearMetodoPagoUseCase crearMetodoPagoUseCase;
    private final EditarMetodoPagoUseCase editarMetodoPagoUseCase;
    private final ListarMetodoPagoUseCase listarMetodoPagoUseCase;
    private final ObtenerMetodoPagoUseCase obtenerMetodoPagoUseCase;
    private final EliminarMetodoPagoUseCase eliminarMetodoPagoUseCase;
    private final CambiarEstadoMetodoPagoUseCase cambiarEstadoMetodoPagoUseCase;

    @PostMapping
    public ResponseEntity<MetodoPagoResponse> crear(@Valid @RequestBody MetodoPagoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MetodoPagoResponse.from(crearMetodoPagoUseCase.create(request.toDomain())));
    }

    @GetMapping
    public ResponseEntity<PageResult<MetodoPagoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoMetodoPago estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        MetodoPagoFiltro filtro = new MetodoPagoFiltro(search, estado);
        return ResponseEntity.ok(listarMetodoPagoUseCase.listar(filtro, page, size).map(MetodoPagoResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(MetodoPagoResponse.from(obtenerMetodoPagoUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodoPagoResponse> editar(@PathVariable Long id,
                                                      @Valid @RequestBody MetodoPagoRequest request) {
        return ResponseEntity.ok(MetodoPagoResponse.from(editarMetodoPagoUseCase.editar(id, request.toDomain())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<MetodoPagoResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(MetodoPagoResponse.from(cambiarEstadoMetodoPagoUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<MetodoPagoResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(MetodoPagoResponse.from(cambiarEstadoMetodoPagoUseCase.desactivar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarMetodoPagoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

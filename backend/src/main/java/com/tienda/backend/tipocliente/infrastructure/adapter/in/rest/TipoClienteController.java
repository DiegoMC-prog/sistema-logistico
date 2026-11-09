package com.tienda.backend.tipocliente.infrastructure.adapter.in.rest;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.tipocliente.application.model.TipoClienteFiltro;
import com.tienda.backend.tipocliente.application.port.in.CambiarEstadoTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.CrearTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.EditarTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.EliminarTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.ListarTipoClienteUseCase;
import com.tienda.backend.tipocliente.application.port.in.ObtenerTipoClienteUseCase;
import com.tienda.backend.tipocliente.domain.model.EstadoTipoCliente;
import com.tienda.backend.tipocliente.infrastructure.adapter.in.rest.dto.TipoClienteRequest;
import com.tienda.backend.tipocliente.infrastructure.adapter.in.rest.dto.TipoClienteResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tipos-cliente")
@RequiredArgsConstructor
public class TipoClienteController {

    private final CrearTipoClienteUseCase crearTipoClienteUseCase;
    private final EditarTipoClienteUseCase editarTipoClienteUseCase;
    private final ListarTipoClienteUseCase listarTipoClienteUseCase;
    private final ObtenerTipoClienteUseCase obtenerTipoClienteUseCase;
    private final EliminarTipoClienteUseCase eliminarTipoClienteUseCase;
    private final CambiarEstadoTipoClienteUseCase cambiarEstadoTipoClienteUseCase;

    @PostMapping
    public ResponseEntity<TipoClienteResponse> crear(@Valid @RequestBody TipoClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TipoClienteResponse.from(crearTipoClienteUseCase.create(request.toDomain())));
    }

    @GetMapping
    public ResponseEntity<PageResult<TipoClienteResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoTipoCliente estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        TipoClienteFiltro filtro = new TipoClienteFiltro(search, estado);
        return ResponseEntity.ok(listarTipoClienteUseCase.listar(filtro, page, size).map(TipoClienteResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(TipoClienteResponse.from(obtenerTipoClienteUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoClienteResponse> editar(@PathVariable Long id,
                                                      @Valid @RequestBody TipoClienteRequest request) {
        return ResponseEntity.ok(TipoClienteResponse.from(editarTipoClienteUseCase.editar(id, request.toDomain())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<TipoClienteResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(TipoClienteResponse.from(cambiarEstadoTipoClienteUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<TipoClienteResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(TipoClienteResponse.from(cambiarEstadoTipoClienteUseCase.desactivar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarTipoClienteUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

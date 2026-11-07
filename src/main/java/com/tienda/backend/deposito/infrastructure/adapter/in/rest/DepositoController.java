package com.tienda.backend.deposito.infrastructure.adapter.in.rest;

import com.tienda.backend.deposito.application.model.DepositoFiltro;
import com.tienda.backend.deposito.application.port.in.CambiarEstadoDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.CrearDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.EditarDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.EliminarDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.ListarDepositoUseCase;
import com.tienda.backend.deposito.application.port.in.ObtenerDepositoUseCase;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.deposito.domain.model.EstadoDeposito;
import com.tienda.backend.deposito.infrastructure.adapter.in.rest.dto.DepositoRequest;
import com.tienda.backend.deposito.infrastructure.adapter.in.rest.dto.DepositoResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/depositos")
@RequiredArgsConstructor
public class DepositoController {

    private final CrearDepositoUseCase crearDepositoUseCase;
    private final EditarDepositoUseCase editarDepositoUseCase;
    private final ListarDepositoUseCase listarDepositoUseCase;
    private final ObtenerDepositoUseCase obtenerDepositoUseCase;
    private final EliminarDepositoUseCase eliminarDepositoUseCase;
    private final CambiarEstadoDepositoUseCase cambiarEstadoDepositoUseCase;

    @PostMapping
    public ResponseEntity<DepositoResponse> crear(@Valid @RequestBody DepositoRequest request) {
        Deposito guardado = crearDepositoUseCase.create(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(DepositoResponse.from(guardado));
    }

    @GetMapping
    public ResponseEntity<PageResult<DepositoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoDeposito estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        DepositoFiltro filtro = new DepositoFiltro(search, estado);
        return ResponseEntity.ok(listarDepositoUseCase.listar(filtro, page, size).map(DepositoResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(DepositoResponse.from(obtenerDepositoUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepositoResponse> editar(@PathVariable Long id, @Valid @RequestBody DepositoRequest request) {
        return ResponseEntity.ok(DepositoResponse.from(editarDepositoUseCase.editar(id, request.toDomain())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<DepositoResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(DepositoResponse.from(cambiarEstadoDepositoUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<DepositoResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(DepositoResponse.from(cambiarEstadoDepositoUseCase.desactivar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarDepositoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

package com.tienda.backend.cliente.infrastructure.adapter.in.rest;

import com.tienda.backend.cliente.application.model.ClienteFiltro;
import com.tienda.backend.cliente.application.port.in.CambiarEstadoClienteUseCase;
import com.tienda.backend.cliente.application.port.in.CrearClienteUseCase;
import com.tienda.backend.cliente.application.port.in.EditarClienteUseCase;
import com.tienda.backend.cliente.application.port.in.EliminarClienteUseCase;
import com.tienda.backend.cliente.application.port.in.ListarClienteUseCase;
import com.tienda.backend.cliente.application.port.in.ObtenerClienteUseCase;
import com.tienda.backend.cliente.domain.model.EstadoCliente;
import com.tienda.backend.cliente.infrastructure.adapter.in.rest.dto.ClienteRequest;
import com.tienda.backend.cliente.infrastructure.adapter.in.rest.dto.ClienteResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final CrearClienteUseCase crearClienteUseCase;
    private final EditarClienteUseCase editarClienteUseCase;
    private final ListarClienteUseCase listarClienteUseCase;
    private final ObtenerClienteUseCase obtenerClienteUseCase;
    private final EliminarClienteUseCase eliminarClienteUseCase;
    private final CambiarEstadoClienteUseCase cambiarEstadoClienteUseCase;

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClienteResponse.from(crearClienteUseCase.create(request.toDomain())));
    }

    @GetMapping
    public ResponseEntity<PageResult<ClienteResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long tipoClienteId,
            @RequestParam(required = false) EstadoCliente estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ClienteFiltro filtro = new ClienteFiltro(search, tipoClienteId, estado);
        return ResponseEntity.ok(listarClienteUseCase.listar(filtro, page, size).map(ClienteResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ClienteResponse.from(obtenerClienteUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> editar(@PathVariable Long id, @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ClienteResponse.from(editarClienteUseCase.editar(id, request.toDomain())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ClienteResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(ClienteResponse.from(cambiarEstadoClienteUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ClienteResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ClienteResponse.from(cambiarEstadoClienteUseCase.desactivar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarClienteUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

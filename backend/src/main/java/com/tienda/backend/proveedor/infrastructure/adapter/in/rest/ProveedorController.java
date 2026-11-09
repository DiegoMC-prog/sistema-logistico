package com.tienda.backend.proveedor.infrastructure.adapter.in.rest;

import com.tienda.backend.proveedor.application.model.ProveedorFiltro;
import com.tienda.backend.proveedor.application.port.in.CambiarEstadoProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.CrearProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.EditarProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.EliminarProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.ListarProveedorUseCase;
import com.tienda.backend.proveedor.application.port.in.ObtenerProveedorUseCase;
import com.tienda.backend.proveedor.domain.model.EstadoProveedor;
import com.tienda.backend.proveedor.infrastructure.adapter.in.rest.dto.ProveedorRequest;
import com.tienda.backend.proveedor.infrastructure.adapter.in.rest.dto.ProveedorResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final CrearProveedorUseCase crearProveedorUseCase;
    private final EditarProveedorUseCase editarProveedorUseCase;
    private final ListarProveedorUseCase listarProveedorUseCase;
    private final ObtenerProveedorUseCase obtenerProveedorUseCase;
    private final EliminarProveedorUseCase eliminarProveedorUseCase;
    private final CambiarEstadoProveedorUseCase cambiarEstadoProveedorUseCase;

    @PostMapping
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProveedorResponse.from(crearProveedorUseCase.create(request.toDomain())));
    }

    @GetMapping
    public ResponseEntity<PageResult<ProveedorResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoProveedor estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProveedorFiltro filtro = new ProveedorFiltro(search, estado);
        return ResponseEntity.ok(listarProveedorUseCase.listar(filtro, page, size).map(ProveedorResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ProveedorResponse.from(obtenerProveedorUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> editar(@PathVariable Long id,
                                                     @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(ProveedorResponse.from(editarProveedorUseCase.editar(id, request.toDomain())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ProveedorResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(ProveedorResponse.from(cambiarEstadoProveedorUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProveedorResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ProveedorResponse.from(cambiarEstadoProveedorUseCase.desactivar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarProveedorUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

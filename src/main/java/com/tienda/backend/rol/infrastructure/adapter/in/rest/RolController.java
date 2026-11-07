package com.tienda.backend.rol.infrastructure.adapter.in.rest;

import com.tienda.backend.rol.application.model.RolFiltro;
import com.tienda.backend.rol.application.port.in.AsignarPermisosRolUseCase;
import com.tienda.backend.rol.application.port.in.CrearRolUseCase;
import com.tienda.backend.rol.application.port.in.EditarRolUseCase;
import com.tienda.backend.rol.application.port.in.EliminarRolUseCase;
import com.tienda.backend.rol.application.port.in.ListarRolUseCase;
import com.tienda.backend.rol.application.port.in.ObtenerRolUseCase;
import com.tienda.backend.rol.infrastructure.adapter.in.rest.dto.AsignarPermisosRequest;
import com.tienda.backend.rol.infrastructure.adapter.in.rest.dto.RolDetalleResponse;
import com.tienda.backend.rol.infrastructure.adapter.in.rest.dto.RolRequest;
import com.tienda.backend.rol.infrastructure.adapter.in.rest.dto.RolResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private final CrearRolUseCase crearRolUseCase;
    private final EditarRolUseCase editarRolUseCase;
    private final ListarRolUseCase listarRolUseCase;
    private final ObtenerRolUseCase obtenerRolUseCase;
    private final EliminarRolUseCase eliminarRolUseCase;
    private final AsignarPermisosRolUseCase asignarPermisosRolUseCase;

    @PostMapping
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RolResponse.from(crearRolUseCase.create(request.toDomain())));
    }

    @GetMapping
    public ResponseEntity<PageResult<RolResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listarRolUseCase.listar(new RolFiltro(search), page, size).map(RolResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolDetalleResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(RolDetalleResponse.from(obtenerRolUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolResponse> editar(@PathVariable Long id, @Valid @RequestBody RolRequest request) {
        return ResponseEntity.ok(RolResponse.from(editarRolUseCase.editar(id, request.toDomain())));
    }

    @PutMapping("/{id}/permisos")
    public ResponseEntity<RolDetalleResponse> asignarPermisos(@PathVariable Long id,
                                                              @Valid @RequestBody AsignarPermisosRequest request) {
        return ResponseEntity.ok(RolDetalleResponse.from(
                asignarPermisosRolUseCase.asignarPermisos(id, request.permisoIds())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarRolUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

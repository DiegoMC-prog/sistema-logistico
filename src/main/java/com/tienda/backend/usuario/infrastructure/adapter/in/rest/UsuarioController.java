package com.tienda.backend.usuario.infrastructure.adapter.in.rest;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.usuario.application.model.UsuarioFiltro;
import com.tienda.backend.usuario.application.port.in.AsignarRolesUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.CambiarEstadoUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.CrearUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.EditarUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.EliminarUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.ListarUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.ObtenerUsuarioUseCase;
import com.tienda.backend.usuario.application.port.in.RestablecerPasswordUsuarioUseCase;
import com.tienda.backend.usuario.domain.model.EstadoUsuario;
import com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto.AsignarRolesRequest;
import com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto.EditarUsuarioRequest;
import com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto.UsuarioRequest;
import com.tienda.backend.usuario.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final CrearUsuarioUseCase crearUsuarioUseCase;
    private final EditarUsuarioUseCase editarUsuarioUseCase;
    private final ListarUsuarioUseCase listarUsuarioUseCase;
    private final ObtenerUsuarioUseCase obtenerUsuarioUseCase;
    private final EliminarUsuarioUseCase eliminarUsuarioUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;
    private final RestablecerPasswordUsuarioUseCase restablecerPasswordUsuarioUseCase;
    private final AsignarRolesUsuarioUseCase asignarRolesUsuarioUseCase;

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.from(crearUsuarioUseCase.create(request.toComando())));
    }

    @GetMapping
    public ResponseEntity<PageResult<UsuarioResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoUsuario estado,
            @RequestParam(required = false) Long rolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UsuarioFiltro filtro = new UsuarioFiltro(search, estado, rolId);
        return ResponseEntity.ok(listarUsuarioUseCase.listar(filtro, page, size).map(UsuarioResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.from(obtenerUsuarioUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> editar(@PathVariable Long id,
                                                  @Valid @RequestBody EditarUsuarioRequest request) {
        return ResponseEntity.ok(UsuarioResponse.from(editarUsuarioUseCase.editar(id, request.toComando())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.from(cambiarEstadoUsuarioUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(UsuarioResponse.from(cambiarEstadoUsuarioUseCase.desactivar(id)));
    }

    /** Genera una nueva contraseña temporal y la envía por correo; la respuesta no la incluye. */
    @PostMapping("/{id}/restablecer-password")
    public ResponseEntity<Void> restablecerPassword(@PathVariable Long id) {
        restablecerPasswordUsuarioUseCase.restablecerPassword(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<UsuarioResponse> asignarRoles(@PathVariable Long id,
                                                        @Valid @RequestBody AsignarRolesRequest request) {
        return ResponseEntity.ok(UsuarioResponse.from(asignarRolesUsuarioUseCase.asignarRoles(id, request.rolIds())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarUsuarioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

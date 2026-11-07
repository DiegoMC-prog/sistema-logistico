package com.tienda.backend.unidadmedida.infrastructure.adapter.in.rest;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.unidadmedida.application.model.UnidadMedidaFiltro;
import com.tienda.backend.unidadmedida.application.port.in.CrearUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.EditarUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.EliminarUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.ListarUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.application.port.in.ObtenerUnidadMedidaUseCase;
import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;
import com.tienda.backend.unidadmedida.infrastructure.adapter.in.rest.dto.UnidadMedidaRequest;
import com.tienda.backend.unidadmedida.infrastructure.adapter.in.rest.dto.UnidadMedidaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/unidades-medida")
@RequiredArgsConstructor
public class UnidadMedidaController {

    private final CrearUnidadMedidaUseCase crearUnidadMedidaUseCase;
    private final EditarUnidadMedidaUseCase editarUnidadMedidaUseCase;
    private final ListarUnidadMedidaUseCase listarUnidadMedidaUseCase;
    private final ObtenerUnidadMedidaUseCase obtenerUnidadMedidaUseCase;
    private final EliminarUnidadMedidaUseCase eliminarUnidadMedidaUseCase;

    @PostMapping
    public ResponseEntity<UnidadMedidaResponse> crear(@Valid @RequestBody UnidadMedidaRequest request) {
        UnidadMedida guardada = crearUnidadMedidaUseCase.create(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(UnidadMedidaResponse.from(guardada));
    }

    @GetMapping
    public ResponseEntity<PageResult<UnidadMedidaResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UnidadMedidaFiltro filtro = new UnidadMedidaFiltro(search);
        return ResponseEntity.ok(listarUnidadMedidaUseCase.listar(filtro, page, size).map(UnidadMedidaResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnidadMedidaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(UnidadMedidaResponse.from(obtenerUnidadMedidaUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnidadMedidaResponse> editar(@PathVariable Long id,
                                                       @Valid @RequestBody UnidadMedidaRequest request) {
        return ResponseEntity.ok(UnidadMedidaResponse.from(editarUnidadMedidaUseCase.editar(id, request.toDomain())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarUnidadMedidaUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

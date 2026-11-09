package com.tienda.backend.presentacion.infrastructure.adapter.in.rest;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.application.port.in.CrearPresentacionUseCase;
import com.tienda.backend.presentacion.application.port.in.EditarPresentacionUseCase;
import com.tienda.backend.presentacion.application.port.in.EliminarPresentacionUseCase;
import com.tienda.backend.presentacion.application.port.in.ListarPresentacionesPorProductoUseCase;
import com.tienda.backend.presentacion.application.port.in.ObtenerPresentacionUseCase;
import com.tienda.backend.presentacion.infrastructure.adapter.in.rest.dto.EditarPresentacionRequest;
import com.tienda.backend.presentacion.infrastructure.adapter.in.rest.dto.PresentacionRequest;
import com.tienda.backend.presentacion.infrastructure.adapter.in.rest.dto.PresentacionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PresentacionController {

    private final CrearPresentacionUseCase crearPresentacionUseCase;
    private final EditarPresentacionUseCase editarPresentacionUseCase;
    private final ListarPresentacionesPorProductoUseCase listarPresentacionesPorProductoUseCase;
    private final ObtenerPresentacionUseCase obtenerPresentacionUseCase;
    private final EliminarPresentacionUseCase eliminarPresentacionUseCase;

    @PostMapping("/productos/{productoId}/presentaciones")
    public ResponseEntity<PresentacionResponse> crear(@PathVariable Long productoId,
                                                      @Valid @RequestBody PresentacionRequest request) {
        PresentacionDetalle guardada = crearPresentacionUseCase.create(request.toDomain(productoId));
        return ResponseEntity.status(HttpStatus.CREATED).body(PresentacionResponse.from(guardada));
    }

    @GetMapping("/productos/{productoId}/presentaciones")
    public ResponseEntity<List<PresentacionResponse>> listarPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(listarPresentacionesPorProductoUseCase.listarPorProducto(productoId).stream()
                .map(PresentacionResponse::from)
                .toList());
    }

    @GetMapping("/presentaciones/{id}")
    public ResponseEntity<PresentacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(PresentacionResponse.from(obtenerPresentacionUseCase.obtener(id)));
    }

    @GetMapping("/presentaciones/codigo-barras/{codigo}")
    public ResponseEntity<PresentacionResponse> obtenerPorCodigoBarras(@PathVariable String codigo) {
        return ResponseEntity.ok(PresentacionResponse.from(obtenerPresentacionUseCase.obtenerPorCodigoBarras(codigo)));
    }

    @PutMapping("/presentaciones/{id}")
    public ResponseEntity<PresentacionResponse> editar(@PathVariable Long id,
                                                       @Valid @RequestBody EditarPresentacionRequest request) {
        return ResponseEntity.ok(PresentacionResponse.from(editarPresentacionUseCase.editar(id, request.toComando())));
    }

    @DeleteMapping("/presentaciones/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarPresentacionUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

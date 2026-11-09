package com.tienda.backend.precio.infrastructure.adapter.in.rest;

import com.tienda.backend.precio.application.model.PrecioFiltro;
import com.tienda.backend.precio.application.port.in.CambiarEstadoPrecioUseCase;
import com.tienda.backend.precio.application.port.in.CrearPrecioUseCase;
import com.tienda.backend.precio.application.port.in.EditarPrecioUseCase;
import com.tienda.backend.precio.application.port.in.EliminarPrecioUseCase;
import com.tienda.backend.precio.application.port.in.ListarPreciosUseCase;
import com.tienda.backend.precio.application.port.in.ObtenerPrecioUseCase;
import com.tienda.backend.precio.application.port.in.ResolverPrecioUseCase;
import com.tienda.backend.precio.domain.model.EstadoPrecio;
import com.tienda.backend.precio.infrastructure.adapter.in.rest.dto.PrecioEditarRequest;
import com.tienda.backend.precio.infrastructure.adapter.in.rest.dto.PrecioRequest;
import com.tienda.backend.precio.infrastructure.adapter.in.rest.dto.PrecioResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PrecioController {

    private final CrearPrecioUseCase crearPrecioUseCase;
    private final EditarPrecioUseCase editarPrecioUseCase;
    private final ListarPreciosUseCase listarPreciosUseCase;
    private final ObtenerPrecioUseCase obtenerPrecioUseCase;
    private final EliminarPrecioUseCase eliminarPrecioUseCase;
    private final CambiarEstadoPrecioUseCase cambiarEstadoPrecioUseCase;
    private final ResolverPrecioUseCase resolverPrecioUseCase;

    @PostMapping("/presentaciones/{presentacionId}/precios")
    public ResponseEntity<PrecioResponse> crear(@PathVariable Long presentacionId,
                                                @Valid @RequestBody PrecioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PrecioResponse.from(crearPrecioUseCase.create(presentacionId, request.toDomain(presentacionId))));
    }

    @GetMapping("/presentaciones/{presentacionId}/precios")
    public ResponseEntity<PageResult<PrecioResponse>> listar(
            @PathVariable Long presentacionId,
            @RequestParam(required = false) Long tipoClienteId,
            @RequestParam(required = false) Boolean soloGenerales,
            @RequestParam(required = false) EstadoPrecio estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PrecioFiltro filtro = new PrecioFiltro(tipoClienteId, soloGenerales, estado);
        return ResponseEntity.ok(listarPreciosUseCase.listar(presentacionId, filtro, page, size).map(PrecioResponse::from));
    }

    @GetMapping("/presentaciones/{presentacionId}/precio-aplicable")
    public ResponseEntity<PrecioResponse> precioAplicable(@PathVariable Long presentacionId,
                                                          @RequestParam(defaultValue = "1") BigDecimal cantidad,
                                                          @RequestParam(required = false) Long tipoClienteId) {
        return ResponseEntity.ok(PrecioResponse.from(resolverPrecioUseCase.resolver(presentacionId, cantidad, tipoClienteId)));
    }

    @GetMapping("/precios/{id}")
    public ResponseEntity<PrecioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(PrecioResponse.from(obtenerPrecioUseCase.obtener(id)));
    }

    @PutMapping("/precios/{id}")
    public ResponseEntity<PrecioResponse> editar(@PathVariable Long id, @Valid @RequestBody PrecioEditarRequest request) {
        return ResponseEntity.ok(PrecioResponse.from(
                editarPrecioUseCase.editar(id, request.cantidadMinima(), request.precio())));
    }

    @PatchMapping("/precios/{id}/activar")
    public ResponseEntity<PrecioResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(PrecioResponse.from(cambiarEstadoPrecioUseCase.activar(id)));
    }

    @PatchMapping("/precios/{id}/desactivar")
    public ResponseEntity<PrecioResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(PrecioResponse.from(cambiarEstadoPrecioUseCase.desactivar(id)));
    }

    @DeleteMapping("/precios/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarPrecioUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

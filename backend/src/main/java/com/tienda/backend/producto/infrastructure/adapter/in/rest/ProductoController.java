package com.tienda.backend.producto.infrastructure.adapter.in.rest;

import com.tienda.backend.producto.application.port.in.CambiarEstadoProductoUseCase;
import com.tienda.backend.producto.application.port.in.CrearProductoUseCase;
import com.tienda.backend.producto.application.port.in.EditarProductoUseCase;
import com.tienda.backend.producto.application.port.in.EliminarProductoUseCase;
import com.tienda.backend.producto.application.port.in.ListarProductoUseCase;
import com.tienda.backend.producto.application.port.in.ObtenerProductoUseCase;
import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.application.model.ProductoFiltro;
import com.tienda.backend.producto.domain.model.EstadoProducto;
import com.tienda.backend.producto.infrastructure.adapter.in.rest.dto.ProductoRequest;
import com.tienda.backend.producto.infrastructure.adapter.in.rest.dto.ProductoResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final CrearProductoUseCase crearProductoUseCase;
    private final EditarProductoUseCase editarProductoUseCase;
    private final ListarProductoUseCase listarProductoUseCase;
    private final ObtenerProductoUseCase obtenerProductoUseCase;
    private final EliminarProductoUseCase eliminarProductoUseCase;
    private final CambiarEstadoProductoUseCase cambiarEstadoProductoUseCase;

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoDetalle guardado = crearProductoUseCase.create(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductoResponse.from(guardado));
    }

    @GetMapping
    public ResponseEntity<PageResult<ProductoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) EstadoProducto estado,
            @RequestParam(required = false) Boolean esPerecedero,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductoFiltro filtro = new ProductoFiltro(search, categoriaId, estado, esPerecedero);
        return ResponseEntity.ok(listarProductoUseCase.listar(filtro, page, size).map(ProductoResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoResponse.from(obtenerProductoUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> editar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ProductoResponse.from(editarProductoUseCase.editar(id, request.toDomain())));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<ProductoResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoResponse.from(cambiarEstadoProductoUseCase.activar(id)));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ProductoResponse> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoResponse.from(cambiarEstadoProductoUseCase.desactivar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarProductoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

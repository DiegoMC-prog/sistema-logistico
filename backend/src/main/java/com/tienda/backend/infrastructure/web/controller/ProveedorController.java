package com.tienda.backend.infrastructure.web.controller;

import com.tienda.backend.application.dto.request.ProveedorRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.ProveedorResponse;
import com.tienda.backend.application.dto.response.ProveedorStatsResponse;
import com.tienda.backend.application.service.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse response = proveedorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<ProveedorStatsResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(proveedorService.obtenerEstadisticas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProveedorResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombreEmpresa
    ) {
        return ResponseEntity.ok(proveedorService.listar(page, size, nombreEmpresa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request
    ) {
        return ResponseEntity.ok(proveedorService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ProveedorResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado
    ) {
        return ResponseEntity.ok(proveedorService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

package com.tienda.backend.infrastructure.web.controller;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;
import com.tienda.backend.application.dto.response.DepositoStatsResponse;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.service.DepositoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/depositos")
public class DepositoController {

    private final DepositoService depositoService;

    public DepositoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @PostMapping
    public ResponseEntity<DepositoResponse> crear(@Valid @RequestBody DepositoRequest request) {
        DepositoResponse response = depositoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<DepositoStatsResponse> obtenerEstadisticas() {
        return ResponseEntity.ok(depositoService.obtenerEstadisticas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(depositoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<DepositoResponse>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre
    ) {
        return ResponseEntity.ok(depositoService.listar(page, size, nombre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepositoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody DepositoRequest request
    ) {
        return ResponseEntity.ok(depositoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<DepositoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean estado
    ) {
        return ResponseEntity.ok(depositoService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        depositoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

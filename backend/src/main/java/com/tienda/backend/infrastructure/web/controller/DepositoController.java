package com.tienda.backend.infrastructure.web.controller;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;
import com.tienda.backend.application.service.DepositoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/v1/depositos")
public class DepositoController {
    private final DepositoService depositoService;

    public DepositoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @PostMapping
    public ResponseEntity<DepositoResponse> crear(@Valid @RequestBody DepositoRequest request) {
        DepositoResponse response = depositoService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/depositos/" + response.id()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(depositoService.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<Page<DepositoResponse>> obtenerTodos(Pageable pageable) {
        return ResponseEntity.ok(depositoService.listarTodo(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepositoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody DepositoRequest request) {
        return ResponseEntity.ok(depositoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<Void> eliminar(@PathVariable Long id) {
        depositoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

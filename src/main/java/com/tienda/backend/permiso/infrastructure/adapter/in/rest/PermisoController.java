package com.tienda.backend.permiso.infrastructure.adapter.in.rest;

import com.tienda.backend.permiso.application.port.in.ListarPermisoUseCase;
import com.tienda.backend.permiso.infrastructure.adapter.in.rest.dto.PermisoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final ListarPermisoUseCase listarPermisoUseCase;

    @GetMapping
    public ResponseEntity<List<PermisoResponse>> listar() {
        return ResponseEntity.ok(listarPermisoUseCase.listar().stream().map(PermisoResponse::from).toList());
    }
}

package com.tienda.backend.precio.infrastructure.adapter.in.rest;

import com.tienda.backend.precio.application.model.CrearRecargoComando;
import com.tienda.backend.precio.application.model.RecargoFiltro;
import com.tienda.backend.precio.application.model.RecargoResumen;
import com.tienda.backend.precio.application.port.in.GestionarRecargosUseCase;
import com.tienda.backend.precio.domain.model.AlcanceRecargo;
import com.tienda.backend.precio.infrastructure.adapter.in.rest.dto.RecargoRequest;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Recargos por escasez (RF-35). Todo exige RECARGO_GESTIONAR (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/recargos-escasez")
@RequiredArgsConstructor
public class RecargoController {

    private final GestionarRecargosUseCase recargosUseCase;

    @GetMapping
    public ResponseEntity<PageResult<RecargoResumen>> listar(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) AlcanceRecargo alcance,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(recargosUseCase.listar(new RecargoFiltro(activo, alcance, search), page, size));
    }

    @PostMapping
    public ResponseEntity<RecargoResumen> crear(@Valid @RequestBody RecargoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recargosUseCase.crear(new CrearRecargoComando(
                request.alcance(), request.productoId(), request.categoriaId(), request.porcentaje(), request.motivo())));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<RecargoResumen> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(recargosUseCase.desactivar(id));
    }
}

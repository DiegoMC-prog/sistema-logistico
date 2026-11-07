package com.tienda.backend.lote.infrastructure.adapter.in.rest;

import com.tienda.backend.lote.application.model.LoteFiltro;
import com.tienda.backend.lote.application.port.in.EditarLoteUseCase;
import com.tienda.backend.lote.application.port.in.EliminarLoteUseCase;
import com.tienda.backend.lote.application.port.in.ListarLoteUseCase;
import com.tienda.backend.lote.application.port.in.ObtenerLoteUseCase;
import com.tienda.backend.lote.infrastructure.adapter.in.rest.dto.EditarLoteRequest;
import com.tienda.backend.lote.infrastructure.adapter.in.rest.dto.LoteResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final EditarLoteUseCase editarLoteUseCase;
    private final ListarLoteUseCase listarLoteUseCase;
    private final ObtenerLoteUseCase obtenerLoteUseCase;
    private final EliminarLoteUseCase eliminarLoteUseCase;

    @GetMapping
    public ResponseEntity<PageResult<LoteResponse>> listar(
            @RequestParam(required = false) Long productoId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate venceDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate venceHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LoteFiltro filtro = new LoteFiltro(productoId, search, venceDesde, venceHasta);
        return ResponseEntity.ok(listarLoteUseCase.listar(filtro, page, size).map(LoteResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(LoteResponse.from(obtenerLoteUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoteResponse> editar(@PathVariable Long id, @Valid @RequestBody EditarLoteRequest request) {
        return ResponseEntity.ok(LoteResponse.from(editarLoteUseCase.editar(id, request.toComando())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        eliminarLoteUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

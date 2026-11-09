package com.tienda.backend.pedido.infrastructure.adapter.in.rest;

import com.tienda.backend.pedido.application.model.PedidoFiltro;
import com.tienda.backend.pedido.application.port.in.ConsultarPedidoUseCase;
import com.tienda.backend.pedido.application.port.in.GestionarPedidoUseCase;
import com.tienda.backend.pedido.application.port.in.RegistrarRecepcionUseCase;
import com.tienda.backend.pedido.domain.model.EstadoPedido;
import com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto.PedidoRequest;
import com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto.PedidoResponse;
import com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto.RecepcionRequest;
import com.tienda.backend.pedido.infrastructure.adapter.in.rest.dto.RecepcionResponse;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final GestionarPedidoUseCase gestionarPedidoUseCase;
    private final ConsultarPedidoUseCase consultarPedidoUseCase;
    private final RegistrarRecepcionUseCase registrarRecepcionUseCase;

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PedidoResponse.from(gestionarPedidoUseCase.crear(request.toComando())));
    }

    @GetMapping
    public ResponseEntity<PageResult<PedidoResponse>> listar(
            @RequestParam(required = false) String numero,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PedidoFiltro filtro = new PedidoFiltro(numero, proveedorId, depositoId, estado, desde, hasta);
        return ResponseEntity.ok(consultarPedidoUseCase.listar(filtro, page, size).map(PedidoResponse::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(PedidoResponse.from(consultarPedidoUseCase.obtener(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponse> editar(@PathVariable Long id, @Valid @RequestBody PedidoRequest request) {
        return ResponseEntity.ok(PedidoResponse.from(gestionarPedidoUseCase.editar(id, request.toComando())));
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<PedidoResponse> enviar(@PathVariable Long id) {
        return ResponseEntity.ok(PedidoResponse.from(gestionarPedidoUseCase.enviar(id)));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(PedidoResponse.from(gestionarPedidoUseCase.cancelar(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        gestionarPedidoUseCase.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/recepciones")
    public ResponseEntity<RecepcionResponse> recibir(@PathVariable Long id,
                                                     @Valid @RequestBody RecepcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RecepcionResponse.from(registrarRecepcionUseCase.registrar(request.toComando(id))));
    }

    @GetMapping("/{id}/recepciones")
    public ResponseEntity<List<RecepcionResponse>> recepciones(@PathVariable Long id) {
        return ResponseEntity.ok(consultarPedidoUseCase.listarRecepciones(id).stream()
                .map(RecepcionResponse::from).toList());
    }
}

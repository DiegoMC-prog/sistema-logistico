package com.tienda.backend.abastecimiento.infrastructure.adapter.in.rest;

import com.tienda.backend.abastecimiento.application.model.CalculoResumen;
import com.tienda.backend.abastecimiento.application.model.CrearPedidosComando;
import com.tienda.backend.abastecimiento.application.model.GuardarParametrosComando;
import com.tienda.backend.abastecimiento.application.model.OrigenCalculo;
import com.tienda.backend.abastecimiento.application.model.ParametrosProducto;
import com.tienda.backend.abastecimiento.application.model.PedidoGenerado;
import com.tienda.backend.abastecimiento.application.model.SugerenciaFiltro;
import com.tienda.backend.abastecimiento.application.model.SugerenciaResumen;
import com.tienda.backend.abastecimiento.application.port.in.ConsultarSugerenciasUseCase;
import com.tienda.backend.abastecimiento.application.port.in.GestionarSugerenciasUseCase;
import com.tienda.backend.abastecimiento.application.port.in.ParametrosReordenUseCase;
import com.tienda.backend.abastecimiento.application.port.in.RecalcularSugerenciasUseCase;
import com.tienda.backend.abastecimiento.domain.model.AccionSugerencia;
import com.tienda.backend.abastecimiento.domain.model.EstadoSugerencia;
import com.tienda.backend.abastecimiento.infrastructure.adapter.in.rest.dto.CrearPedidosRequest;
import com.tienda.backend.abastecimiento.infrastructure.adapter.in.rest.dto.ParametrosReordenRequest;
import com.tienda.backend.shared.application.model.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sugerencias de reorden (RF-34). Consultar exige REORDEN_VER o REORDEN_GESTIONAR; recalcular, convertir en pedido,
 * descartar y editar parámetros exigen REORDEN_GESTIONAR (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/abastecimiento")
@RequiredArgsConstructor
public class AbastecimientoController {

    private final RecalcularSugerenciasUseCase recalcularUseCase;
    private final ConsultarSugerenciasUseCase consultarUseCase;
    private final GestionarSugerenciasUseCase gestionarUseCase;
    private final ParametrosReordenUseCase parametrosUseCase;

    /** Último recálculo; 204 si todavía no se ejecutó ninguno. */
    @GetMapping("/calculo")
    public ResponseEntity<CalculoResumen> ultimoCalculo() {
        return consultarUseCase.ultimoCalculo().map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/calculo")
    public ResponseEntity<CalculoResumen> recalcular() {
        return ResponseEntity.status(HttpStatus.CREATED).body(recalcularUseCase.recalcular(OrigenCalculo.MANUAL));
    }

    @GetMapping("/sugerencias")
    public ResponseEntity<PageResult<SugerenciaResumen>> listar(
            @RequestParam(required = false) EstadoSugerencia estado,
            @RequestParam(required = false) AccionSugerencia accion,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultarUseCase.listar(new SugerenciaFiltro(estado, accion, proveedorId, search), page, size));
    }

    @GetMapping("/sugerencias/{id}")
    public ResponseEntity<SugerenciaResumen> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(consultarUseCase.obtener(id));
    }

    /** Convierte sugerencias en pedidos BORRADOR (uno por proveedor). */
    @PostMapping("/pedidos")
    public ResponseEntity<List<PedidoGenerado>> crearPedidos(@Valid @RequestBody CrearPedidosRequest request) {
        CrearPedidosComando comando = new CrearPedidosComando(request.depositoDestinoId(), request.escenario(),
                request.observacion(), request.items().stream()
                .map(i -> new CrearPedidosComando.Item(i.sugerenciaId(), i.cantidad())).toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(gestionarUseCase.crearPedidos(comando));
    }

    @PatchMapping("/sugerencias/{id}/descartar")
    public ResponseEntity<SugerenciaResumen> descartar(@PathVariable Long id) {
        return ResponseEntity.ok(gestionarUseCase.descartar(id));
    }

    @GetMapping("/parametros/{productoId}")
    public ResponseEntity<ParametrosProducto> parametros(@PathVariable Long productoId) {
        return ResponseEntity.ok(parametrosUseCase.obtenerParametros(productoId));
    }

    @PutMapping("/parametros/{productoId}")
    public ResponseEntity<ParametrosProducto> guardarParametros(@PathVariable Long productoId,
                                                                @Valid @RequestBody ParametrosReordenRequest request) {
        return ResponseEntity.ok(parametrosUseCase.guardarParametros(productoId, new GuardarParametrosComando(
                request.costoPedido(), request.costoAlmacenPct(), request.nivelServicio(), request.proveedorId(),
                request.activo() == null || request.activo())));
    }
}

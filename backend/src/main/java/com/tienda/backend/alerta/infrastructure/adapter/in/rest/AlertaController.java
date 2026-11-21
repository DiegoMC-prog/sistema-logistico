package com.tienda.backend.alerta.infrastructure.adapter.in.rest;

import com.tienda.backend.alerta.application.model.AlertaFiltro;
import com.tienda.backend.alerta.application.model.AlertaResumen;
import com.tienda.backend.alerta.application.model.ConteoAlertas;
import com.tienda.backend.alerta.application.model.ResultadoGeneracion;
import com.tienda.backend.alerta.application.port.in.ConsultarAlertasUseCase;
import com.tienda.backend.alerta.application.port.in.GenerarAlertasUseCase;
import com.tienda.backend.alerta.application.port.in.MarcarAlertaLeidaUseCase;
import com.tienda.backend.alerta.domain.model.TipoAlerta;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Alertas de vencimiento y stock bajo (RF-13, RF-16). Todo exige ALERTA_VER (ver SecurityConfig). */
@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final ConsultarAlertasUseCase consultarAlertasUseCase;
    private final MarcarAlertaLeidaUseCase marcarAlertaLeidaUseCase;
    private final GenerarAlertasUseCase generarAlertasUseCase;

    /** Alertas activas (o todas con incluirResueltas=true), primero las no leídas y las más recientes. */
    @GetMapping
    public ResponseEntity<PageResult<AlertaResumen>> listar(
            @RequestParam(required = false) Boolean leida,
            @RequestParam(required = false) TipoAlerta tipo,
            @RequestParam(defaultValue = "false") boolean incluirResueltas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultarAlertasUseCase.listar(new AlertaFiltro(leida, tipo, incluirResueltas), page, size));
    }

    /** Contador de la campana: alertas activas sin leer. */
    @GetMapping("/conteo")
    public ResponseEntity<ConteoAlertas> conteo() {
        return ResponseEntity.ok(consultarAlertasUseCase.conteo());
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<AlertaResumen> marcarLeida(@PathVariable Long id) {
        return ResponseEntity.ok(marcarAlertaLeidaUseCase.marcarLeida(id));
    }

    @PatchMapping("/leer-todas")
    public ResponseEntity<Map<String, Integer>> marcarTodasLeidas() {
        return ResponseEntity.ok(Map.of("marcadas", marcarAlertaLeidaUseCase.marcarTodasLeidas()));
    }

    /** Ejecuta la revisión en el momento, sin esperar a la tarea diaria. Es idempotente. */
    @PostMapping("/generar")
    public ResponseEntity<ResultadoGeneracion> generar() {
        return ResponseEntity.ok(generarAlertasUseCase.generar());
    }
}

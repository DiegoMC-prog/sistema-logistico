package com.tienda.backend.reporte.infrastructure.adapter.in.rest;

import com.tienda.backend.reporte.application.model.Panel;
import com.tienda.backend.reporte.application.port.in.ConsultarPanelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Panel de inicio. Lo puede pedir cualquier usuario autenticado; solo recibe los bloques de los reportes que puede
 * ver. {@code bloques} lista los incluidos y los demás simplemente no vienen (no se envían como {@code null}).
 */
@RestController
@RequestMapping("/api/v1/reportes/panel")
@RequiredArgsConstructor
public class ReportePanelController {

    private final ConsultarPanelUseCase consultarPanelUseCase;

    @GetMapping
    public ResponseEntity<Map<String, Object>> panel() {
        Panel panel = consultarPanelUseCase.consultar();

        Map<String, Object> bloques = new LinkedHashMap<>();
        agregar(bloques, "ventas", panel.ventas());
        agregar(bloques, "caja", panel.caja());
        agregar(bloques, "inventario", panel.inventario());
        agregar(bloques, "compras", panel.compras());
        agregar(bloques, "utilidad", panel.utilidad());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("generadoEn", panel.generadoEn());
        respuesta.put("bloques", new ArrayList<>(bloques.keySet()));
        respuesta.putAll(bloques);
        return ResponseEntity.ok(respuesta);
    }

    private static void agregar(Map<String, Object> destino, String nombre, Object bloque) {
        if (bloque != null) {
            destino.put(nombre, bloque);
        }
    }
}

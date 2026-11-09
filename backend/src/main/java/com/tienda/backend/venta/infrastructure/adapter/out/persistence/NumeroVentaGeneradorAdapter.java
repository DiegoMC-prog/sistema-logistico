package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.port.out.NumeroVentaGeneradorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Genera {@code V-00000001} con una secuencia de la BD, segura ante ventas simultáneas (puede haber saltos). */
@Component
@RequiredArgsConstructor
public class NumeroVentaGeneradorAdapter implements NumeroVentaGeneradorPort {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public String siguiente() {
        Long secuencia = jdbcTemplate.queryForObject("SELECT nextval('seq_numero_venta')", Long.class);
        return String.format("V-%08d", secuencia);
    }
}

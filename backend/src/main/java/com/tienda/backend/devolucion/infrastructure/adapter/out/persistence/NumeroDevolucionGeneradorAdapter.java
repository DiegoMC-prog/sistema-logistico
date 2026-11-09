package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.application.port.out.NumeroDevolucionGeneradorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Genera {@code D-00000001} con una secuencia de la BD, segura ante operaciones simultáneas (puede haber saltos). */
@Component
@RequiredArgsConstructor
public class NumeroDevolucionGeneradorAdapter implements NumeroDevolucionGeneradorPort {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public String siguiente() {
        Long secuencia = jdbcTemplate.queryForObject("SELECT nextval('seq_numero_devolucion')", Long.class);
        return String.format("D-%08d", secuencia);
    }
}

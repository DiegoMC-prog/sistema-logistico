package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import com.tienda.backend.pedido.application.port.out.NumeroPedidoGeneradorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Genera {@code PP-00000001} con una secuencia de la BD, segura ante pedidos simultáneos (puede haber saltos). */
@Component
@RequiredArgsConstructor
public class NumeroPedidoGeneradorAdapter implements NumeroPedidoGeneradorPort {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public String siguiente() {
        Long secuencia = jdbcTemplate.queryForObject("SELECT nextval('seq_numero_pedido_proveedor')", Long.class);
        return String.format("PP-%08d", secuencia);
    }
}

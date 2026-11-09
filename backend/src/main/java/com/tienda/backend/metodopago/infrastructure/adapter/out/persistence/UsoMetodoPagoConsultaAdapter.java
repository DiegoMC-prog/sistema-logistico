package com.tienda.backend.metodopago.infrastructure.adapter.out.persistence;

import com.tienda.backend.metodopago.application.port.out.UsoMetodoPagoConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Comprueba si el método de pago ya se usó en pagos de ventas o en devoluciones consultando las tablas directamente.
 * Cuando existan los módulos de ventas y devoluciones, este adaptador pasa a usar sus repositorios.
 */
@Component
@RequiredArgsConstructor
public class UsoMetodoPagoConsultaAdapter implements UsoMetodoPagoConsultaPort {
    private static final String SQL = """
            SELECT EXISTS (SELECT 1 FROM venta_pagos WHERE metodo_pago_id = ?)
                OR EXISTS (SELECT 1 FROM devoluciones_venta WHERE metodo_pago_id = ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean estaEnUso(Long metodoPagoId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(SQL, Boolean.class, metodoPagoId, metodoPagoId));
    }
}

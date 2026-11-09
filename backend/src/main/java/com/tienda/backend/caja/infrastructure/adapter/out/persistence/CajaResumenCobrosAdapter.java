package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.application.model.CobroPorMetodo;
import com.tienda.backend.caja.application.port.out.ResumenCobrosTurnoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Suma los pagos de las ventas del turno consultando las tablas directamente, hasta que exista el módulo de ventas;
 * entonces pasa a usar su repositorio y el servicio de caja no cambia.
 */
@Component
@RequiredArgsConstructor
public class CajaResumenCobrosAdapter implements ResumenCobrosTurnoPort {
    private static final String SQL = """
            SELECT mp.id, mp.nombre, COALESCE(SUM(vp.monto), 0)
            FROM venta_pagos vp
            JOIN ventas v ON v.id = vp.venta_id
            JOIN metodos_pago mp ON mp.id = vp.metodo_pago_id
            WHERE v.turno_id = ? AND v.estado = 'COMPLETADA'
            GROUP BY mp.id, mp.nombre
            ORDER BY mp.nombre
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<CobroPorMetodo> cobrosPorMetodo(Long turnoId) {
        return jdbcTemplate.query(SQL,
                (rs, i) -> new CobroPorMetodo(rs.getLong(1), rs.getString(2), rs.getBigDecimal(3)), turnoId);
    }

    private static final String SQL_REEMBOLSOS = """
            SELECT COALESCE(SUM(d.monto_reembolsado), 0)
            FROM devoluciones_venta d
            JOIN metodos_pago mp ON mp.id = d.metodo_pago_id
            WHERE d.turno_id = ? AND mp.nombre = 'EFECTIVO'
            """;

    @Override
    public java.math.BigDecimal reembolsosEfectivo(Long turnoId) {
        return jdbcTemplate.queryForObject(SQL_REEMBOLSOS, java.math.BigDecimal.class, turnoId);
    }

    private static final String SQL_ABONOS = """
            SELECT COALESCE(SUM(a.monto), 0)
            FROM abonos_credito a
            JOIN metodos_pago mp ON mp.id = a.metodo_pago_id
            WHERE a.turno_id = ? AND mp.nombre = 'EFECTIVO'
            """;

    @Override
    public java.math.BigDecimal abonosEfectivo(Long turnoId) {
        return jdbcTemplate.queryForObject(SQL_ABONOS, java.math.BigDecimal.class, turnoId);
    }
}

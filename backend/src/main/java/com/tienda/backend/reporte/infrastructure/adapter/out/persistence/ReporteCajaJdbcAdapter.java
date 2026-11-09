package com.tienda.backend.reporte.infrastructure.adapter.out.persistence;

import com.tienda.backend.reporte.application.model.FilasCaja;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.out.ReporteCajaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Cierre de caja por turno. El esperado es el mismo que calcula el módulo de caja: apertura + ingresos − egresos +
 * ventas cobradas en efectivo − reembolsos en efectivo. Las ventas anuladas no cuentan. Un turno cerrado conserva el
 * esperado que se congeló al cerrarlo.
 */
@Component
@RequiredArgsConstructor
public class ReporteCajaJdbcAdapter implements ReporteCajaRepositoryPort {

    private final JdbcTemplate jdbc;

    @Override
    public List<FilasCaja.Cierre> cierres(RangoFechas rango, Long cajaId, Long usuarioId, String estado) {
        List<Object> args = new ArrayList<>();
        args.add(Timestamp.valueOf(rango.inicio()));
        args.add(Timestamp.valueOf(rango.finExclusivo()));
        StringBuilder filtros = new StringBuilder();
        if (cajaId != null) {
            filtros.append(" AND t.caja_id = ?");
            args.add(cajaId);
        }
        if (usuarioId != null) {
            filtros.append(" AND t.usuario_id = ?");
            args.add(usuarioId);
        }
        if (estado != null) {
            filtros.append(" AND t.estado = ?");
            args.add(estado);
        }

        return jdbc.query("""
                SELECT t.id, c.id, c.nombre, u.id, u.nombre, t.estado, t.fecha_apertura, t.fecha_cierre,
                       t.monto_apertura, t.monto_cierre_esperado, t.monto_cierre_declarado,
                       (SELECT COUNT(*) FROM ventas v WHERE v.turno_id = t.id AND v.estado = 'COMPLETADA'),
                       (SELECT COALESCE(SUM(v.total), 0) FROM ventas v
                         WHERE v.turno_id = t.id AND v.estado = 'COMPLETADA'),
                       (SELECT COALESCE(SUM(pg.monto), 0) FROM venta_pagos pg
                         JOIN ventas v ON v.id = pg.venta_id JOIN metodos_pago mp ON mp.id = pg.metodo_pago_id
                         WHERE v.turno_id = t.id AND v.estado = 'COMPLETADA' AND mp.nombre = 'EFECTIVO'),
                       (SELECT COALESCE(SUM(pg.monto), 0) FROM venta_pagos pg
                         JOIN ventas v ON v.id = pg.venta_id JOIN metodos_pago mp ON mp.id = pg.metodo_pago_id
                         WHERE v.turno_id = t.id AND v.estado = 'COMPLETADA'
                           AND mp.nombre NOT IN ('EFECTIVO', 'CREDITO_DEVOLUCION', 'CREDITO')),
                       (SELECT COALESCE(SUM(mc.monto), 0) FROM movimientos_caja mc
                         WHERE mc.turno_id = t.id AND mc.tipo = 'INGRESO'),
                       (SELECT COALESCE(SUM(mc.monto), 0) FROM movimientos_caja mc
                         WHERE mc.turno_id = t.id AND mc.tipo = 'EGRESO'),
                       (SELECT COALESCE(SUM(d.monto_reembolsado), 0) FROM devoluciones_venta d
                         JOIN metodos_pago mp ON mp.id = d.metodo_pago_id
                         WHERE d.turno_id = t.id AND mp.nombre = 'EFECTIVO'),
                       (SELECT COALESCE(SUM(ab.monto), 0) FROM abonos_credito ab
                         JOIN metodos_pago mp ON mp.id = ab.metodo_pago_id
                         WHERE ab.turno_id = t.id AND mp.nombre = 'EFECTIVO')
                FROM turnos_caja t
                JOIN cajas c ON c.id = t.caja_id
                JOIN usuarios u ON u.id = t.usuario_id
                WHERE t.fecha_apertura >= ? AND t.fecha_apertura < ?%s
                ORDER BY t.fecha_apertura DESC, t.id DESC
                """.formatted(filtros), (rs, n) -> {
            BigDecimal apertura = rs.getBigDecimal(9);
            BigDecimal esperadoGuardado = rs.getBigDecimal(10);
            BigDecimal declarado = rs.getBigDecimal(11);
            BigDecimal efectivo = rs.getBigDecimal(14);
            BigDecimal ingresos = rs.getBigDecimal(16);
            BigDecimal egresos = rs.getBigDecimal(17);
            BigDecimal reembolsos = rs.getBigDecimal(18);
            BigDecimal abonos = rs.getBigDecimal(19);

            boolean cerrado = "CERRADO".equals(rs.getString(6));
            BigDecimal esperado = cerrado && esperadoGuardado != null ? esperadoGuardado
                    : apertura.add(ingresos).subtract(egresos).add(efectivo).add(abonos).subtract(reembolsos);
            Timestamp cierre = rs.getTimestamp(8);

            return new FilasCaja.Cierre(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getLong(4), rs.getString(5),
                    rs.getString(6), rs.getTimestamp(7).toLocalDateTime(), cierre == null ? null : cierre.toLocalDateTime(),
                    m(apertura), rs.getLong(12), m(rs.getBigDecimal(13)), m(efectivo), m(rs.getBigDecimal(15)),
                    m(ingresos), m(egresos), m(reembolsos), m(esperado),
                    cerrado && declarado != null ? m(declarado) : null,
                    cerrado && declarado != null ? m(declarado.subtract(esperado)) : null, m(abonos));
        }, args.toArray());
    }

    private static BigDecimal m(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }
}

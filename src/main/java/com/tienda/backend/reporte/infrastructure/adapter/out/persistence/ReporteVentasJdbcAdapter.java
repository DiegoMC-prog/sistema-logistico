package com.tienda.backend.reporte.infrastructure.adapter.out.persistence;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasVentas;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.out.ReporteVentasRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes de ventas con consultas de agregación directas (no se cargan entidades). Reglas comunes:
 * una venta cuenta como tal solo si está COMPLETADA; las anuladas se informan aparte; los rangos se aplican
 * a la fecha de la venta (o de la devolución, o de la anulación, según el dato).
 */
@Component
@RequiredArgsConstructor
public class ReporteVentasJdbcAdapter implements ReporteVentasRepositoryPort {
    private static final String CREDITO = "CREDITO_DEVOLUCION";
    private static final String COMPLETADA = "v.estado = 'COMPLETADA'";
    private static final String RANGO_VENTA = "v.fecha_venta >= ? AND v.fecha_venta < ?";
    private static final String RANGO_DEVOLUCION = "d.fecha >= ? AND d.fecha < ?";

    private final JdbcTemplate jdbc;

    // ------------------------------------------------------------------ resumen por período

    @Override
    public List<FilasVentas.Periodo> resumenPorPeriodo(RangoFechas rango, Agrupacion agrupacion) {
        String unidad = agrupacion.unidadSql();   // valor fijo del enum, no del cliente
        Object[] p = parametros(rango);

        Map<LocalDate, Acumulado> porPeriodo = new LinkedHashMap<>();
        for (LocalDate d = agrupacion.inicioDe(rango.desde()); !d.isAfter(rango.hasta()); d = agrupacion.siguiente(d)) {
            porPeriodo.put(d, new Acumulado());
        }

        jdbc.query("""
                SELECT date_trunc('%s', v.fecha_venta)::date,
                       COUNT(*) FILTER (WHERE v.estado = 'COMPLETADA'),
                       COALESCE(SUM(v.subtotal) FILTER (WHERE v.estado = 'COMPLETADA'), 0),
                       COALESCE(SUM(v.descuento_total) FILTER (WHERE v.estado = 'COMPLETADA'), 0),
                       COALESCE(SUM(v.total) FILTER (WHERE v.estado = 'COMPLETADA'), 0),
                       COUNT(*) FILTER (WHERE v.estado = 'ANULADA'),
                       COALESCE(SUM(v.total) FILTER (WHERE v.estado = 'ANULADA'), 0)
                FROM ventas v WHERE %s GROUP BY 1
                """.formatted(unidad, RANGO_VENTA), rs -> {
            Acumulado a = porPeriodo.computeIfAbsent(rs.getObject(1, LocalDate.class), k -> new Acumulado());
            a.ventas = rs.getLong(2);
            a.subtotal = rs.getBigDecimal(3);
            a.descuentos = rs.getBigDecimal(4);
            a.total = rs.getBigDecimal(5);
            a.anuladas = rs.getLong(6);
            a.totalAnuladas = rs.getBigDecimal(7);
        }, p);

        jdbc.query("""
                SELECT date_trunc('%s', d.fecha)::date, COUNT(*), COALESCE(SUM(d.monto_reembolsado), 0)
                FROM devoluciones_venta d WHERE %s GROUP BY 1
                """.formatted(unidad, RANGO_DEVOLUCION), rs -> {
            Acumulado a = porPeriodo.computeIfAbsent(rs.getObject(1, LocalDate.class), k -> new Acumulado());
            a.devoluciones = rs.getLong(2);
            a.devuelto = a.devuelto.add(rs.getBigDecimal(3));
        }, p);

        return porPeriodo.entrySet().stream()
                .map(e -> e.getValue().aFila(e.getKey()))
                .toList();
    }

    /** Acumula los datos de un período mientras se leen las tres consultas. */
    private static final class Acumulado {
        long ventas;
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descuentos = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        long anuladas;
        BigDecimal totalAnuladas = BigDecimal.ZERO;
        long devoluciones;
        BigDecimal devuelto = BigDecimal.ZERO;

        FilasVentas.Periodo aFila(LocalDate periodo) {
            return new FilasVentas.Periodo(periodo, ventas, m(subtotal), m(descuentos), m(total),
                    promedio(total, ventas), anuladas, m(totalAnuladas), devoluciones, m(devuelto),
                    m(total.subtract(devuelto)));
        }
    }

    // ------------------------------------------------------------------ por producto

    @Override
    public List<FilasVentas.Producto> porProducto(RangoFechas rango, Long categoriaId, int limite) {
        List<Object> args = new ArrayList<>(List.of(parametros(rango)));
        String filtroCategoria = "";
        if (categoriaId != null) {
            filtroCategoria = " AND p.categoria_id = ?";
            args.add(categoriaId);
        }
        args.add(limite);
        return jdbc.query("""
                SELECT p.id, p.nombre, c.nombre,
                       COALESCE(SUM(d.cantidad * d.factor_conversion), 0),
                       COUNT(DISTINCT v.id),
                       COALESCE(SUM(d.descuento), 0),
                       COALESCE(SUM(d.subtotal), 0)
                FROM venta_detalle d
                JOIN ventas v ON v.id = d.venta_id
                JOIN productos p ON p.id = d.producto_id
                JOIN categorias c ON c.id = p.categoria_id
                WHERE %s AND %s%s
                GROUP BY p.id, p.nombre, c.nombre
                ORDER BY 7 DESC, p.nombre
                LIMIT ?
                """.formatted(COMPLETADA, RANGO_VENTA, filtroCategoria),
                (rs, i) -> new FilasVentas.Producto(rs.getLong(1), rs.getString(2), rs.getString(3),
                        m(rs.getBigDecimal(4)), rs.getLong(5), m(rs.getBigDecimal(6)), m(rs.getBigDecimal(7))),
                args.toArray());
    }

    @Override
    public List<FilasVentas.ProductoSinVentas> productosSinVentas(RangoFechas rango, Long categoriaId, int limite) {
        // Orden de los marcadores en el SQL: categoría (si hay), desde, hasta, límite
        List<Object> args = new ArrayList<>();
        String filtroCategoria = "";
        if (categoriaId != null) {
            filtroCategoria = " AND p.categoria_id = ?";
            args.add(categoriaId);
        }
        args.addAll(List.of(parametros(rango)));
        args.add(limite);
        return jdbc.query("""
                SELECT p.id, p.nombre, c.nombre
                FROM productos p
                JOIN categorias c ON c.id = p.categoria_id
                WHERE p.deleted_at IS NULL AND p.estado = 'ACTIVO'%s
                  AND NOT EXISTS (
                      SELECT 1 FROM venta_detalle d JOIN ventas v ON v.id = d.venta_id
                      WHERE d.producto_id = p.id AND %s AND %s)
                ORDER BY p.nombre
                LIMIT ?
                """.formatted(filtroCategoria, COMPLETADA, RANGO_VENTA),
                (rs, i) -> new FilasVentas.ProductoSinVentas(rs.getLong(1), rs.getString(2), rs.getString(3)),
                args.toArray());
    }

    // ------------------------------------------------------------------ por cajero y por caja

    @Override
    public List<FilasVentas.Origen> porCajero(RangoFechas rango) {
        return origen(rango,
                "SELECT u.id, u.nombre, %s FROM ventas v JOIN usuarios u ON u.id = v.usuario_id WHERE " + RANGO_VENTA
                        + " GROUP BY u.id, u.nombre",
                "SELECT d.usuario_id, COUNT(*), COALESCE(SUM(d.monto_reembolsado), 0) FROM devoluciones_venta d WHERE "
                        + RANGO_DEVOLUCION + " GROUP BY d.usuario_id",
                "SELECT id, nombre FROM usuarios WHERE id IN (%s)");
    }

    @Override
    public List<FilasVentas.Origen> porCaja(RangoFechas rango) {
        return origen(rango,
                "SELECT c.id, c.nombre, %s FROM ventas v JOIN turnos_caja t ON t.id = v.turno_id "
                        + "JOIN cajas c ON c.id = t.caja_id WHERE " + RANGO_VENTA + " GROUP BY c.id, c.nombre",
                "SELECT t.caja_id, COUNT(*), COALESCE(SUM(d.monto_reembolsado), 0) FROM devoluciones_venta d "
                        + "JOIN turnos_caja t ON t.id = d.turno_id WHERE " + RANGO_DEVOLUCION + " GROUP BY t.caja_id",
                "SELECT id, nombre FROM cajas WHERE id IN (%s)");
    }

    private static final String AGREGADOS_VENTA = """
            COUNT(*) FILTER (WHERE v.estado = 'COMPLETADA'),
            COALESCE(SUM(v.total) FILTER (WHERE v.estado = 'COMPLETADA'), 0),
            COUNT(*) FILTER (WHERE v.estado = 'ANULADA'),
            COALESCE(SUM(v.total) FILTER (WHERE v.estado = 'ANULADA'), 0)""";

    /** Une ventas y devoluciones por el id del cajero o de la caja; quien solo tuvo devoluciones también aparece. */
    private List<FilasVentas.Origen> origen(RangoFechas rango, String sqlVentas, String sqlDevoluciones,
                                            String sqlNombres) {
        Object[] p = parametros(rango);
        Map<Long, Object[]> filas = new LinkedHashMap<>();   // id -> {nombre, ventas, total, anuladas, totalAnuladas, devs, devuelto}

        jdbc.query(sqlVentas.formatted(AGREGADOS_VENTA), rs -> {
            filas.put(rs.getLong(1), new Object[]{rs.getString(2), rs.getLong(3), rs.getBigDecimal(4), rs.getLong(5),
                    rs.getBigDecimal(6), 0L, BigDecimal.ZERO});
        }, p);

        Map<Long, long[]> devs = new HashMap<>();
        Map<Long, BigDecimal> montos = new HashMap<>();
        jdbc.query(sqlDevoluciones, rs -> {
            devs.put(rs.getLong(1), new long[]{rs.getLong(2)});
            montos.put(rs.getLong(1), rs.getBigDecimal(3));
        }, p);

        List<Long> faltantes = devs.keySet().stream().filter(id -> !filas.containsKey(id)).toList();
        if (!faltantes.isEmpty()) {
            String marcas = String.join(",", faltantes.stream().map(x -> "?").toList());
            jdbc.query(sqlNombres.formatted(marcas), rs -> {
                filas.put(rs.getLong(1), new Object[]{rs.getString(2), 0L, BigDecimal.ZERO, 0L, BigDecimal.ZERO, 0L,
                        BigDecimal.ZERO});
            }, faltantes.toArray());
        }

        List<FilasVentas.Origen> resultado = new ArrayList<>();
        filas.forEach((id, f) -> {
            long ventas = (long) f[1];
            BigDecimal total = (BigDecimal) f[2];
            resultado.add(new FilasVentas.Origen(id, (String) f[0], ventas, m(total), promedio(total, ventas),
                    (long) f[3], m((BigDecimal) f[4]), devs.containsKey(id) ? devs.get(id)[0] : 0L,
                    m(montos.getOrDefault(id, BigDecimal.ZERO))));
        });
        resultado.sort(Comparator.comparing(FilasVentas.Origen::total).reversed());
        return resultado;
    }

    // ------------------------------------------------------------------ por método de pago

    @Override
    public List<FilasVentas.MetodoPago> porMetodoPago(RangoFechas rango) {
        Object[] p = parametros(rango);
        Map<Long, Object[]> filas = new LinkedHashMap<>();   // id -> {nombre, ventas, cobrado, reembolsado}

        jdbc.query("""
                SELECT m.id, m.nombre, COUNT(DISTINCT v.id), COALESCE(SUM(pg.monto), 0)
                FROM venta_pagos pg
                JOIN ventas v ON v.id = pg.venta_id
                JOIN metodos_pago m ON m.id = pg.metodo_pago_id
                WHERE %s AND m.nombre <> '%s' AND %s
                GROUP BY m.id, m.nombre
                """.formatted(COMPLETADA, CREDITO, RANGO_VENTA), rs -> {
            filas.put(rs.getLong(1), new Object[]{rs.getString(2), rs.getLong(3), rs.getBigDecimal(4), BigDecimal.ZERO});
        }, p);

        // Reembolsos en dinero o a tarjeta. El crédito de un cambio (método CREDITO_DEVOLUCION) no es dinero.
        jdbc.query("""
                SELECT m.id, m.nombre, COALESCE(SUM(d.monto_reembolsado), 0)
                FROM devoluciones_venta d
                JOIN metodos_pago m ON m.id = d.metodo_pago_id
                WHERE d.monto_reembolsado > 0 AND m.nombre <> '%s' AND %s
                GROUP BY m.id, m.nombre
                """.formatted(CREDITO, RANGO_DEVOLUCION), rs -> {
            long id = rs.getLong(1);
            String nombre = rs.getString(2);
            Object[] f = filas.computeIfAbsent(id, k -> new Object[]{nombre, 0L, BigDecimal.ZERO, BigDecimal.ZERO});
            f[3] = rs.getBigDecimal(3);
        }, p);

        // En un cambio, lo que el crédito no cubre de lo devuelto se entrega en efectivo (sale de la caja)
        BigDecimal saldoCambios = jdbc.queryForObject("""
                SELECT COALESCE(SUM(d.monto_reembolsado - COALESCE(cred.monto, 0)), 0)
                FROM devoluciones_venta d
                LEFT JOIN (SELECT pg.venta_id, SUM(pg.monto) AS monto
                           FROM venta_pagos pg JOIN metodos_pago m ON m.id = pg.metodo_pago_id
                           WHERE m.nombre = '%s' GROUP BY pg.venta_id) cred ON cred.venta_id = d.venta_cambio_id
                WHERE d.venta_cambio_id IS NOT NULL AND %s
                """.formatted(CREDITO, RANGO_DEVOLUCION), BigDecimal.class, p);
        if (saldoCambios != null && saldoCambios.signum() > 0) {
            jdbc.query("SELECT id, nombre FROM metodos_pago WHERE nombre = 'EFECTIVO'", rs -> {
                long id = rs.getLong(1);
                String nombre = rs.getString(2);
                Object[] f = filas.computeIfAbsent(id, k -> new Object[]{nombre, 0L, BigDecimal.ZERO, BigDecimal.ZERO});
                f[3] = ((BigDecimal) f[3]).add(saldoCambios);
            });
        }

        List<FilasVentas.MetodoPago> resultado = new ArrayList<>();
        filas.forEach((id, f) -> {
            BigDecimal cobrado = (BigDecimal) f[2];
            BigDecimal reembolsado = (BigDecimal) f[3];
            resultado.add(new FilasVentas.MetodoPago(id, (String) f[0], (long) f[1], m(cobrado), m(reembolsado),
                    m(cobrado.subtract(reembolsado))));
        });
        resultado.sort(Comparator.comparing(FilasVentas.MetodoPago::neto).reversed());
        return resultado;
    }

    // ------------------------------------------------------------------ por cliente

    @Override
    public List<FilasVentas.Cliente> porCliente(RangoFechas rango, int limite) {
        List<Object> args = new ArrayList<>(List.of(parametros(rango)));
        args.add(limite);
        return jdbc.query("""
                SELECT c.id, COALESCE(c.nombre, 'Ventas sin cliente'), c.documento, COUNT(*), SUM(v.total),
                       MAX(v.fecha_venta)
                FROM ventas v LEFT JOIN clientes c ON c.id = v.cliente_id
                WHERE %s AND %s
                GROUP BY c.id, c.nombre, c.documento
                ORDER BY SUM(v.total) DESC, 2
                LIMIT ?
                """.formatted(COMPLETADA, RANGO_VENTA), (rs, i) -> filaCliente(rs), args.toArray());
    }

    @Override
    public List<FilasVentas.Cliente> porTipoCliente(RangoFechas rango) {
        return jdbc.query("""
                SELECT t.id, COALESCE(t.nombre, 'Sin tipo'), NULL, COUNT(*), SUM(v.total), MAX(v.fecha_venta)
                FROM ventas v LEFT JOIN tipos_cliente t ON t.id = v.tipo_cliente_id
                WHERE %s AND %s
                GROUP BY t.id, t.nombre
                ORDER BY SUM(v.total) DESC, 2
                """.formatted(COMPLETADA, RANGO_VENTA), (rs, i) -> filaCliente(rs), parametros(rango));
    }

    private static FilasVentas.Cliente filaCliente(java.sql.ResultSet rs) throws java.sql.SQLException {
        long id = rs.getLong(1);
        Long idONulo = rs.wasNull() ? null : id;
        long ventas = rs.getLong(4);
        BigDecimal total = rs.getBigDecimal(5);
        Timestamp ultima = rs.getTimestamp(6);
        return new FilasVentas.Cliente(idONulo, rs.getString(2), rs.getString(3), ventas, m(total),
                promedio(total, ventas), ultima == null ? null : ultima.toLocalDateTime());
    }

    // ------------------------------------------------------------------ anulaciones, devoluciones y cambios

    @Override
    public List<FilasVentas.Incidencia> incidencias(RangoFechas rango, int limite) {
        Object[] p = parametros(rango);
        List<FilasVentas.Incidencia> todas = new ArrayList<>();

        todas.addAll(jdbc.query("""
                SELECT v.numero_venta, v.fecha_anulacion, v.anulada_por, v.motivo_anulacion, v.total
                FROM ventas v
                WHERE v.estado = 'ANULADA' AND v.fecha_anulacion >= ? AND v.fecha_anulacion < ?
                ORDER BY v.fecha_anulacion DESC LIMIT ?
                """, (rs, i) -> new FilasVentas.Incidencia("ANULACION", rs.getString(1), rs.getString(1),
                rs.getTimestamp(2).toLocalDateTime(), rs.getString(3), rs.getString(4), m(rs.getBigDecimal(5))),
                p[0], p[1], limite));

        todas.addAll(jdbc.query("""
                SELECT d.numero_devolucion, d.fecha, u.nombre, d.motivo, d.monto_reembolsado, v.numero_venta,
                       d.venta_cambio_id
                FROM devoluciones_venta d
                JOIN usuarios u ON u.id = d.usuario_id
                JOIN ventas v ON v.id = d.venta_id
                WHERE %s
                ORDER BY d.fecha DESC LIMIT ?
                """.formatted(RANGO_DEVOLUCION), (rs, i) -> {
            rs.getLong(7);
            String tipo = rs.wasNull() ? "DEVOLUCION" : "CAMBIO";
            return new FilasVentas.Incidencia(tipo, rs.getString(1), rs.getString(6),
                    rs.getTimestamp(2).toLocalDateTime(), rs.getString(3), rs.getString(4), m(rs.getBigDecimal(5)));
        }, p[0], p[1], limite));

        todas.sort(Comparator.comparing(FilasVentas.Incidencia::fecha).reversed());
        return todas.size() > limite ? new ArrayList<>(todas.subList(0, limite)) : todas;
    }

    // ------------------------------------------------------------------ utilidades

    private static Object[] parametros(RangoFechas rango) {
        return new Object[]{rango.inicio(), rango.finExclusivo()};
    }

    private static BigDecimal m(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal promedio(BigDecimal total, long cantidad) {
        return cantidad == 0 ? BigDecimal.ZERO.setScale(2)
                : total.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
    }
}

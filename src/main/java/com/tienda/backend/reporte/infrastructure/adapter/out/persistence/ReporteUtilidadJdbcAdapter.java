package com.tienda.backend.reporte.infrastructure.adapter.out.persistence;

import com.tienda.backend.reporte.application.model.Agrupacion;
import com.tienda.backend.reporte.application.model.FilasUtilidad;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.out.ReporteUtilidadRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad = ingreso − costo real. El ingreso sale de {@code venta_detalle.subtotal} (neto de descuentos) y el costo de
 * {@code venta_detalle_lote}, que guarda el costo unitario del lote del que salió cada unidad en el momento de la
 * venta; así un cambio posterior en el costo del lote no altera la utilidad histórica. Solo ventas COMPLETADAS.
 */
@Component
@RequiredArgsConstructor
public class ReporteUtilidadJdbcAdapter implements ReporteUtilidadRepositoryPort {
    private static final String RANGO_VENTA = "v.fecha_venta >= ? AND v.fecha_venta < ?";
    private static final String RANGO_DEVOLUCION = "dev.fecha >= ? AND dev.fecha < ?";

    /** Costo total de una línea de venta (suma de sus lotes); se une con LATERAL para no duplicar el ingreso. */
    private static final String COSTO_DE_LINEA = """
            LEFT JOIN LATERAL (SELECT SUM(dl.cantidad * dl.costo_unitario) AS costo
                               FROM venta_detalle_lote dl WHERE dl.venta_detalle_id = d.id) cl ON TRUE""";

    private final JdbcTemplate jdbc;

    // ------------------------------------------------------------------ por producto

    @Override
    public List<FilasUtilidad.Producto> porProducto(RangoFechas rango, Long categoriaId, int limite) {
        List<Object> args = new ArrayList<>(List.of(rango.inicio(), rango.finExclusivo()));
        String filtroCategoria = "";
        if (categoriaId != null) {
            filtroCategoria = " AND p.categoria_id = ?";
            args.add(categoriaId);
        }
        args.add(limite);
        return jdbc.query("""
                SELECT p.id, p.nombre, c.nombre,
                       COALESCE(SUM(d.cantidad * d.factor_conversion), 0),
                       COALESCE(SUM(d.subtotal), 0),
                       COALESCE(SUM(cl.costo), 0)
                FROM venta_detalle d
                JOIN ventas v ON v.id = d.venta_id
                JOIN productos p ON p.id = d.producto_id
                JOIN categorias c ON c.id = p.categoria_id
                %s
                WHERE v.estado = 'COMPLETADA' AND %s%s
                GROUP BY p.id, p.nombre, c.nombre
                ORDER BY COALESCE(SUM(d.subtotal), 0) - COALESCE(SUM(cl.costo), 0) DESC, p.nombre
                LIMIT ?
                """.formatted(COSTO_DE_LINEA, RANGO_VENTA, filtroCategoria), (rs, n) -> {
            BigDecimal ingreso = rs.getBigDecimal(5);
            BigDecimal costo = rs.getBigDecimal(6);
            BigDecimal utilidad = ingreso.subtract(costo);
            return new FilasUtilidad.Producto(rs.getLong(1), rs.getString(2), rs.getString(3), m(rs.getBigDecimal(4)),
                    m(ingreso), m(costo), m(utilidad), margen(utilidad, ingreso));
        }, args.toArray());
    }

    // ------------------------------------------------------------------ por período

    /** Acumula los datos de un período mientras se leen las tres consultas. */
    private static final class Acumulado {
        long ventas;
        BigDecimal ingreso = BigDecimal.ZERO;
        BigDecimal costo = BigDecimal.ZERO;
        BigDecimal devuelto = BigDecimal.ZERO;
        BigDecimal costoRecuperado = BigDecimal.ZERO;

        FilasUtilidad.Periodo aFila(LocalDate periodo) {
            BigDecimal utilidad = ingreso.subtract(costo);
            BigDecimal neta = ingreso.subtract(devuelto).subtract(costo.subtract(costoRecuperado));
            return new FilasUtilidad.Periodo(periodo, ventas, m(ingreso), m(costo), m(utilidad), margen(utilidad, ingreso),
                    m(devuelto), m(costoRecuperado), m(neta));
        }
    }

    @Override
    public List<FilasUtilidad.Periodo> porPeriodo(RangoFechas rango, Agrupacion agrupacion) {
        String unidad = agrupacion.unidadSql();   // valor fijo del enum, nunca del cliente
        Object[] p = {rango.inicio(), rango.finExclusivo()};

        Map<LocalDate, Acumulado> porPeriodo = new LinkedHashMap<>();
        for (LocalDate d = agrupacion.inicioDe(rango.desde()); !d.isAfter(rango.hasta()); d = agrupacion.siguiente(d)) {
            porPeriodo.put(d, new Acumulado());
        }

        jdbc.query("""
                SELECT date_trunc('%s', v.fecha_venta)::date, COUNT(DISTINCT v.id),
                       COALESCE(SUM(d.subtotal), 0), COALESCE(SUM(cl.costo), 0)
                FROM ventas v
                JOIN venta_detalle d ON d.venta_id = v.id
                %s
                WHERE v.estado = 'COMPLETADA' AND %s
                GROUP BY 1
                """.formatted(unidad, COSTO_DE_LINEA, RANGO_VENTA), rs -> {
            Acumulado a = porPeriodo.computeIfAbsent(rs.getObject(1, LocalDate.class), k -> new Acumulado());
            a.ventas = rs.getLong(2);
            a.ingreso = rs.getBigDecimal(3);
            a.costo = rs.getBigDecimal(4);
        }, p);

        jdbc.query("""
                SELECT date_trunc('%s', dev.fecha)::date, COALESCE(SUM(dev.monto_reembolsado), 0)
                FROM devoluciones_venta dev WHERE %s GROUP BY 1
                """.formatted(unidad, RANGO_DEVOLUCION), rs -> {
            porPeriodo.computeIfAbsent(rs.getObject(1, LocalDate.class), k -> new Acumulado()).devuelto = rs.getBigDecimal(2);
        }, p);

        // Costo de la mercadería devuelta que volvió al stock (la dañada no vuelve: su costo queda como pérdida)
        jdbc.query("""
                SELECT date_trunc('%s', dev.fecha)::date, COALESCE(SUM(dvd.cantidad * dl.costo_unitario), 0)
                FROM devolucion_venta_detalle dvd
                JOIN devoluciones_venta dev ON dev.id = dvd.devolucion_id
                JOIN venta_detalle_lote dl ON dl.venta_detalle_id = dvd.venta_detalle_id AND dl.lote_id = dvd.lote_id
                WHERE dvd.reingresa_stock AND %s
                GROUP BY 1
                """.formatted(unidad, RANGO_DEVOLUCION), rs -> {
            porPeriodo.computeIfAbsent(rs.getObject(1, LocalDate.class), k -> new Acumulado()).costoRecuperado = rs.getBigDecimal(2);
        }, p);

        return porPeriodo.entrySet().stream().map(e -> e.getValue().aFila(e.getKey())).toList();
    }

    // ------------------------------------------------------------------ precios manuales

    @Override
    public List<FilasUtilidad.PrecioManual> preciosManuales(RangoFechas rango, int limite) {
        return jdbc.query("""
                SELECT v.numero_venta, v.estado, v.fecha_venta, u.nombre, p.nombre, d.cantidad, d.precio_unitario,
                       d.subtotal, d.motivo_precio, COALESCE(cl.costo, 0) / d.cantidad
                FROM venta_detalle d
                JOIN ventas v ON v.id = d.venta_id
                JOIN usuarios u ON u.id = v.usuario_id
                JOIN productos p ON p.id = d.producto_id
                %s
                WHERE d.es_precio_modificado AND %s
                ORDER BY v.fecha_venta DESC, d.id DESC
                LIMIT ?
                """.formatted(COSTO_DE_LINEA, RANGO_VENTA), (rs, n) -> {
            BigDecimal cantidad = rs.getBigDecimal(6);
            BigDecimal precio = rs.getBigDecimal(7);
            BigDecimal costo = rs.getBigDecimal(10);
            boolean bajoCosto = precio.compareTo(costo) < 0;
            BigDecimal perdida = bajoCosto ? costo.subtract(precio).multiply(cantidad) : BigDecimal.ZERO;
            return new FilasUtilidad.PrecioManual(rs.getString(1), rs.getString(2), rs.getTimestamp(3).toLocalDateTime(),
                    rs.getString(4), rs.getString(5), cantidad.stripTrailingZeros().scale() < 0 ? cantidad.setScale(0)
                    : cantidad.stripTrailingZeros(), m(precio), m(costo), m(precio.subtract(costo)), bajoCosto,
                    m(perdida), m(rs.getBigDecimal(8)), rs.getString(9));
        }, rango.inicio(), rango.finExclusivo(), limite);
    }

    // ------------------------------------------------------------------ utilidades

    private static BigDecimal m(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Utilidad sobre ingreso en porcentaje; nulo si no hubo ingreso. */
    private static BigDecimal margen(BigDecimal utilidad, BigDecimal ingreso) {
        return ingreso == null || ingreso.signum() == 0 ? null
                : utilidad.multiply(BigDecimal.valueOf(100)).divide(ingreso, 2, RoundingMode.HALF_UP);
    }
}

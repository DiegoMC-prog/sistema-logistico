package com.tienda.backend.reporte.infrastructure.adapter.out.persistence;

import com.tienda.backend.reporte.application.model.AgrupacionStock;
import com.tienda.backend.reporte.application.model.FilasInventario;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.out.ReporteInventarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reportes de inventario con consultas de agregación directas. El stock sale de {@code inventarios} (una fila por
 * depósito y lote) y se valora con el costo de compra del lote; las cantidades están en unidad base.
 */
@Component
@RequiredArgsConstructor
public class ReporteInventarioJdbcAdapter implements ReporteInventarioRepositoryPort {
    private static final BigDecimal TREINTA = BigDecimal.valueOf(30);

    private final JdbcTemplate jdbc;

    // ------------------------------------------------------------------ stock valorizado

    @Override
    public List<FilasInventario.Stock> stockValorizado(AgrupacionStock agrupacion, Long depositoId, Long categoriaId) {
        List<Object> args = new ArrayList<>();
        StringBuilder filtros = new StringBuilder();
        if (depositoId != null) {
            filtros.append(" AND i.deposito_id = ?");
            args.add(depositoId);
        }
        if (categoriaId != null) {
            filtros.append(" AND p.categoria_id = ?");
            args.add(categoriaId);
        }

        // Los campos de agrupación salen de un enum, nunca del cliente
        String columnas;
        String grupo;
        switch (agrupacion) {
            case DEPOSITO -> {
                columnas = "d.id, d.nombre, NULL";
                grupo = "d.id, d.nombre";
            }
            case CATEGORIA -> {
                columnas = "c.id, c.nombre, NULL";
                grupo = "c.id, c.nombre";
            }
            default -> {
                columnas = "p.id, p.nombre, c.nombre";
                grupo = "p.id, p.nombre, c.nombre";
            }
        }
        return jdbc.query("""
                SELECT %s,
                       SUM(i.stock_actual),
                       SUM(i.stock_actual * l.costo_unitario_compra),
                       COUNT(DISTINCT l.id)
                FROM inventarios i
                JOIN lotes l ON l.id = i.lote_id
                JOIN productos p ON p.id = l.producto_id
                JOIN categorias c ON c.id = p.categoria_id
                JOIN depositos d ON d.id = i.deposito_id
                WHERE i.stock_actual > 0 AND i.deleted_at IS NULL AND l.deleted_at IS NULL%s
                GROUP BY %s
                ORDER BY SUM(i.stock_actual * l.costo_unitario_compra) DESC, 2
                """.formatted(columnas, filtros, grupo), (rs, n) -> {
            BigDecimal stock = rs.getBigDecimal(4);
            BigDecimal valor = rs.getBigDecimal(5);
            return new FilasInventario.Stock(rs.getLong(1), rs.getString(2), rs.getString(3), cant(stock), m(valor),
                    stock.signum() == 0 ? BigDecimal.ZERO.setScale(2)
                            : valor.divide(stock, 2, RoundingMode.HALF_UP), rs.getLong(6));
        }, args.toArray());
    }

    // ------------------------------------------------------------------ stock bajo el mínimo

    @Override
    public List<FilasInventario.StockBajo> stockBajo(Long depositoId, Long categoriaId) {
        List<Object> args = new ArrayList<>();
        String filtroStock = "";
        String filtroVentas = "";
        String filtroCategoria = "";

        // Orden de los marcadores: stock (depósito), ventas (fecha y depósito), categoría
        if (depositoId != null) {
            filtroStock = " AND i.deposito_id = ?";
            args.add(depositoId);
        }
        args.add(Timestamp.valueOf(LocalDateTime.now().minusDays(30)));
        if (depositoId != null) {
            filtroVentas = " AND v.deposito_id = ?";
            args.add(depositoId);
        }
        if (categoriaId != null) {
            filtroCategoria = " AND p.categoria_id = ?";
            args.add(categoriaId);
        }

        return jdbc.query("""
                SELECT p.id, p.nombre, c.nombre, COALESCE(s.stock, 0), p.stock_minimo, COALESCE(vt.vendido, 0)
                FROM productos p
                JOIN categorias c ON c.id = p.categoria_id
                LEFT JOIN (SELECT l.producto_id, SUM(i.stock_actual) AS stock
                           FROM inventarios i JOIN lotes l ON l.id = i.lote_id
                           WHERE i.deleted_at IS NULL%s
                           GROUP BY l.producto_id) s ON s.producto_id = p.id
                LEFT JOIN (SELECT d.producto_id, SUM(d.cantidad * d.factor_conversion) AS vendido
                           FROM venta_detalle d JOIN ventas v ON v.id = d.venta_id
                           WHERE v.estado = 'COMPLETADA' AND v.fecha_venta >= ?%s
                           GROUP BY d.producto_id) vt ON vt.producto_id = p.id
                WHERE p.deleted_at IS NULL AND p.estado = 'ACTIVO' AND p.stock_minimo > 0
                  AND COALESCE(s.stock, 0) < p.stock_minimo%s
                ORDER BY COALESCE(s.stock, 0) / p.stock_minimo, p.nombre
                """.formatted(filtroStock, filtroVentas, filtroCategoria), (rs, n) -> {
            BigDecimal stock = rs.getBigDecimal(4);
            BigDecimal minimo = rs.getBigDecimal(5);
            BigDecimal vendido = rs.getBigDecimal(6);
            // Ritmo de venta diario = vendido en 30 días / 30; días de stock = stock / ritmo
            BigDecimal dias = vendido.signum() == 0 ? null
                    : stock.multiply(TREINTA).divide(vendido, 1, RoundingMode.HALF_UP);
            return new FilasInventario.StockBajo(rs.getLong(1), rs.getString(2), rs.getString(3), cant(stock),
                    cant(minimo), cant(minimo.subtract(stock)), cant(vendido), dias);
        }, args.toArray());
    }

    // ------------------------------------------------------------------ lotes por vencer

    @Override
    public List<FilasInventario.LoteVencimiento> lotesPorVencer(LocalDate hoy, LocalDate hasta,
                                                                boolean incluirVencidos, Long depositoId) {
        List<Object> args = new ArrayList<>();
        args.add(java.sql.Date.valueOf(hasta));
        StringBuilder filtros = new StringBuilder();
        if (!incluirVencidos) {
            filtros.append(" AND l.fecha_vencimiento >= ?");
            args.add(java.sql.Date.valueOf(hoy));
        }
        if (depositoId != null) {
            filtros.append(" AND i.deposito_id = ?");
            args.add(depositoId);
        }
        return jdbc.query("""
                SELECT l.id, l.numero_lote, p.id, p.nombre, l.fecha_vencimiento, SUM(i.stock_actual),
                       l.costo_unitario_compra, l.porcentaje_descuento_vencimiento
                FROM inventarios i
                JOIN lotes l ON l.id = i.lote_id
                JOIN productos p ON p.id = l.producto_id
                WHERE i.stock_actual > 0 AND i.deleted_at IS NULL AND l.deleted_at IS NULL
                  AND l.fecha_vencimiento IS NOT NULL AND l.fecha_vencimiento <= ?%s
                GROUP BY l.id, l.numero_lote, p.id, p.nombre, l.fecha_vencimiento, l.costo_unitario_compra,
                         l.porcentaje_descuento_vencimiento
                ORDER BY l.fecha_vencimiento, p.nombre, l.numero_lote
                """.formatted(filtros), (rs, n) -> {
            LocalDate vence = rs.getObject(5, LocalDate.class);
            long dias = ChronoUnit.DAYS.between(hoy, vence);
            BigDecimal stock = rs.getBigDecimal(6);
            BigDecimal costo = rs.getBigDecimal(7);
            return new FilasInventario.LoteVencimiento(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getString(4),
                    vence, dias, dias < 0 ? "VENCIDO" : "POR_VENCER", cant(stock), m(costo), m(stock.multiply(costo)),
                    m(rs.getBigDecimal(8)));
        }, args.toArray());
    }

    // ------------------------------------------------------------------ kardex

    @Override
    public Optional<String> nombreProducto(Long productoId) {
        return jdbc.query("SELECT nombre FROM productos WHERE id = ? AND deleted_at IS NULL",
                (rs, n) -> rs.getString(1), productoId).stream().findFirst();
    }

    /** Lo que el movimiento suma al depósito consultado (o al total si no se eligió depósito). */
    private static String entrada(Long depositoId) {
        return depositoId == null
                ? "CASE WHEN m.tipo_movimiento IN ('ENTRADA', 'AJUSTE_POSITIVO') THEN md.cantidad ELSE 0 END"
                : "CASE WHEN m.deposito_destino_id = " + depositoId.longValue() + " THEN md.cantidad ELSE 0 END";
    }

    /** Lo que el movimiento resta al depósito consultado (o al total si no se eligió depósito). */
    private static String salida(Long depositoId) {
        return depositoId == null
                ? "CASE WHEN m.tipo_movimiento IN ('SALIDA', 'AJUSTE_NEGATIVO') THEN md.cantidad ELSE 0 END"
                : "CASE WHEN m.deposito_origen_id = " + depositoId.longValue() + " THEN md.cantidad ELSE 0 END";
    }

    private static String filtroDeposito(Long depositoId) {
        return depositoId == null ? ""
                : " AND (m.deposito_origen_id = " + depositoId.longValue()
                + " OR m.deposito_destino_id = " + depositoId.longValue() + ")";
    }

    @Override
    public BigDecimal saldoKardexInicial(Long productoId, Long depositoId, RangoFechas rango) {
        BigDecimal saldo = jdbc.queryForObject("""
                SELECT COALESCE(SUM(%s - %s), 0)
                FROM movimiento_inventario_detalle md
                JOIN movimientos_inventarios m ON m.id = md.movimiento_id
                WHERE md.producto_id = ? AND m.fecha_movimiento < ?%s
                """.formatted(entrada(depositoId), salida(depositoId), filtroDeposito(depositoId)),
                BigDecimal.class, productoId, Timestamp.valueOf(rango.inicio()));
        return saldo == null ? BigDecimal.ZERO : saldo;
    }

    @Override
    public List<FilasInventario.Kardex> movimientosKardex(Long productoId, Long depositoId, RangoFechas rango) {
        return jdbc.query("""
                SELECT m.id, m.fecha_movimiento, m.tipo_movimiento, o.nombre, d.nombre, u.nombre,
                       m.referencia_tipo, m.referencia_id, l.numero_lote, %s, %s
                FROM movimiento_inventario_detalle md
                JOIN movimientos_inventarios m ON m.id = md.movimiento_id
                LEFT JOIN depositos o ON o.id = m.deposito_origen_id
                LEFT JOIN depositos d ON d.id = m.deposito_destino_id
                JOIN usuarios u ON u.id = m.usuario_id
                LEFT JOIN lotes l ON l.id = md.lote_id
                WHERE md.producto_id = ? AND m.fecha_movimiento >= ? AND m.fecha_movimiento < ?%s
                ORDER BY m.fecha_movimiento, md.id
                """.formatted(entrada(depositoId), salida(depositoId), filtroDeposito(depositoId)), (rs, n) -> {
            String refTipo = rs.getString(7);
            long refId = rs.getLong(8);
            String referencia = refTipo == null ? null : refTipo + (rs.wasNull() ? "" : " #" + refId);
            return new FilasInventario.Kardex(rs.getLong(1), rs.getTimestamp(2).toLocalDateTime(), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6), referencia, rs.getString(9),
                    cant(rs.getBigDecimal(10)), cant(rs.getBigDecimal(11)), BigDecimal.ZERO);
        }, productoId, Timestamp.valueOf(rango.inicio()), Timestamp.valueOf(rango.finExclusivo()));
    }

    // ------------------------------------------------------------------ utilidades

    /** Cantidad en unidad base: hasta 4 decimales, sin ceros de relleno. */
    private static BigDecimal cant(BigDecimal valor) {
        BigDecimal v = (valor == null ? BigDecimal.ZERO : valor).stripTrailingZeros();
        return v.scale() < 0 ? v.setScale(0) : v;
    }

    private static BigDecimal m(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }
}

package com.tienda.backend.alerta.infrastructure.adapter.out.persistence;

import com.tienda.backend.alerta.application.model.AlertaFiltro;
import com.tienda.backend.alerta.application.model.AlertaResumen;
import com.tienda.backend.alerta.application.model.ConteoAlertas;
import com.tienda.backend.alerta.application.port.out.AlertaRepositoryPort;
import com.tienda.backend.alerta.domain.model.TipoAlerta;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistencia de alertas con SQL directo. La generación es por conjuntos ({@code INSERT ... SELECT}) y se apoya en los
 * índices únicos parciales de la tabla ({@code ON CONFLICT DO NOTHING}) para no duplicar avisos.
 */
@Component
@RequiredArgsConstructor
public class AlertaJdbcAdapter implements AlertaRepositoryPort {

    private static final String SELECT = """
            SELECT a.id, a.tipo, a.producto_id, p.nombre AS producto, a.lote_id, l.numero_lote, a.fecha_referencia,
                   a.cantidad, a.mensaje, a.fecha, a.leida, a.leida_por, a.leida_at, a.resuelta_at
            FROM alertas a
            JOIN productos p ON p.id = a.producto_id
            LEFT JOIN lotes l ON l.id = a.lote_id
            """;

    private static final RowMapper<AlertaResumen> ALERTA = (rs, i) -> {
        Timestamp leidaAt = rs.getTimestamp("leida_at");
        long loteId = rs.getLong("lote_id");
        Long lote = rs.wasNull() ? null : loteId;
        return new AlertaResumen(rs.getLong("id"), TipoAlerta.valueOf(rs.getString("tipo")), rs.getLong("producto_id"),
                rs.getString("producto"), lote, rs.getString("numero_lote"),
                rs.getObject("fecha_referencia", LocalDate.class), rs.getBigDecimal("cantidad").stripTrailingZeros(),
                rs.getString("mensaje"), rs.getTimestamp("fecha").toLocalDateTime(), rs.getBoolean("leida"),
                rs.getString("leida_por"), leidaAt == null ? null : leidaAt.toLocalDateTime(),
                rs.getTimestamp("resuelta_at") != null);
    };

    /** Stock total de cada lote (todos los depósitos), solo lotes con existencias. */
    private static final String STOCK_POR_LOTE = """
            SELECT i.lote_id, SUM(i.stock_actual) AS stock
            FROM inventarios i
            WHERE i.deleted_at IS NULL AND i.stock_actual > 0
            GROUP BY i.lote_id""";

    /** Stock total de cada producto (todos los depósitos y lotes), igual que el reporte de stock bajo. */
    private static final String STOCK_POR_PRODUCTO = """
            SELECT l.producto_id, SUM(i.stock_actual) AS stock
            FROM inventarios i JOIN lotes l ON l.id = i.lote_id
            WHERE i.deleted_at IS NULL
            GROUP BY l.producto_id""";

    private final JdbcTemplate jdbc;

    /** Cantidad sin ceros sobrantes para el texto del mensaje (12.5000 -> 12.5, 12.0000 -> 12). */
    private static String num(String columna) {
        return "rtrim(to_char(" + columna + ", 'FM999999999990.####'), '.')";
    }

    // ------------------------------------------------------------------ consultas

    @Override
    public PageResult<AlertaResumen> listar(AlertaFiltro filtro, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (!filtro.incluirResueltas()) {
            where.append(" AND a.resuelta_at IS NULL");
        }
        if (filtro.leida() != null) {
            where.append(" AND a.leida = ?");
            args.add(filtro.leida());
        }
        if (filtro.tipo() != null) {
            where.append(" AND a.tipo = ?");
            args.add(filtro.tipo().name());
        }

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM alertas a" + where, Long.class, args.toArray());
        long totalElements = total == null ? 0 : total;

        List<Object> argsPagina = new ArrayList<>(args);
        argsPagina.add(size);
        argsPagina.add((long) page * size);
        List<AlertaResumen> contenido = jdbc.query(SELECT + where
                        + " ORDER BY a.leida, a.resuelta_at NULLS FIRST, a.fecha DESC, a.id DESC LIMIT ? OFFSET ?",
                ALERTA, argsPagina.toArray());

        int totalPages = (int) ((totalElements + size - 1) / size);
        return new PageResult<>(contenido, page, size, totalElements, totalPages);
    }

    @Override
    public ConteoAlertas conteoNoLeidas() {
        return jdbc.queryForObject("""
                SELECT COUNT(*),
                       COUNT(*) FILTER (WHERE tipo = 'VENCIMIENTO'),
                       COUNT(*) FILTER (WHERE tipo = 'VENCIDO'),
                       COUNT(*) FILTER (WHERE tipo = 'STOCK_BAJO')
                FROM alertas WHERE leida = FALSE AND resuelta_at IS NULL""",
                (rs, i) -> new ConteoAlertas(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)));
    }

    @Override
    public Optional<AlertaResumen> buscar(Long id) {
        return jdbc.query(SELECT + " WHERE a.id = ?", ALERTA, id).stream().findFirst();
    }

    @Override
    public boolean marcarLeida(Long id, String usuario) {
        return jdbc.update("""
                UPDATE alertas SET leida = TRUE, leida_por = ?, leida_at = CURRENT_TIMESTAMP
                WHERE id = ? AND leida = FALSE""", usuario, id) > 0;
    }

    @Override
    public int marcarTodasLeidas(String usuario) {
        return jdbc.update("""
                UPDATE alertas SET leida = TRUE, leida_por = ?, leida_at = CURRENT_TIMESTAMP
                WHERE leida = FALSE AND resuelta_at IS NULL""", usuario);
    }

    // ------------------------------------------------------------------ generación

    @Override
    public int crearAlertasPorVencer(LocalDate hoy, LocalDate hasta) {
        return jdbc.update("""
                INSERT INTO alertas (tipo, producto_id, lote_id, fecha_referencia, cantidad, mensaje)
                SELECT 'VENCIMIENTO', l.producto_id, l.id, l.fecha_vencimiento, s.stock,
                       left('El lote ' || l.numero_lote || ' de ' || p.nombre || ' vence el '
                            || to_char(l.fecha_vencimiento, 'DD/MM/YYYY') || ' (en ' || (l.fecha_vencimiento - ?::date)
                            || ' días) y tiene ' || %s || ' unidades en stock.', 500)
                FROM lotes l
                JOIN productos p ON p.id = l.producto_id
                JOIN (%s) s ON s.lote_id = l.id
                WHERE l.deleted_at IS NULL AND p.deleted_at IS NULL
                  AND l.fecha_vencimiento BETWEEN ? AND ?
                ON CONFLICT (tipo, lote_id) WHERE lote_id IS NOT NULL DO NOTHING
                """.formatted(num("s.stock"), STOCK_POR_LOTE), Date.valueOf(hoy), Date.valueOf(hoy), Date.valueOf(hasta));
    }

    @Override
    public int crearAlertasVencidos(LocalDate hoy) {
        return jdbc.update("""
                INSERT INTO alertas (tipo, producto_id, lote_id, fecha_referencia, cantidad, mensaje)
                SELECT 'VENCIDO', l.producto_id, l.id, l.fecha_vencimiento, s.stock,
                       left('El lote ' || l.numero_lote || ' de ' || p.nombre || ' venció el '
                            || to_char(l.fecha_vencimiento, 'DD/MM/YYYY') || ' y todavía tiene ' || %s
                            || ' unidades en stock: retírelo de la venta o regístrelo como merma.', 500)
                FROM lotes l
                JOIN productos p ON p.id = l.producto_id
                JOIN (%s) s ON s.lote_id = l.id
                WHERE l.deleted_at IS NULL AND p.deleted_at IS NULL
                  AND l.fecha_vencimiento < ?
                ON CONFLICT (tipo, lote_id) WHERE lote_id IS NOT NULL DO NOTHING
                """.formatted(num("s.stock"), STOCK_POR_LOTE), Date.valueOf(hoy));
    }

    @Override
    public int crearAlertasStockBajo() {
        return jdbc.update("""
                INSERT INTO alertas (tipo, producto_id, cantidad, mensaje)
                SELECT 'STOCK_BAJO', p.id, COALESCE(s.stock, 0),
                       left('El stock de ' || p.nombre || ' es ' || %s || ' unidades, por debajo del mínimo de '
                            || %s || '.', 500)
                FROM productos p
                LEFT JOIN (%s) s ON s.producto_id = p.id
                WHERE p.deleted_at IS NULL AND p.estado = 'ACTIVO' AND p.stock_minimo > 0
                  AND COALESCE(s.stock, 0) < p.stock_minimo
                ON CONFLICT (producto_id) WHERE tipo = 'STOCK_BAJO' AND resuelta_at IS NULL DO NOTHING
                """.formatted(num("COALESCE(s.stock, 0)"), num("p.stock_minimo"), STOCK_POR_PRODUCTO));
    }

    @Override
    public int resolverObsoletas(LocalDate hoy) {
        // Lote agotado o eliminado: ya no hay nada que vender ni retirar
        int lotes = jdbc.update("""
                UPDATE alertas a SET resuelta_at = CURRENT_TIMESTAMP
                WHERE a.resuelta_at IS NULL AND a.lote_id IS NOT NULL
                  AND NOT EXISTS (SELECT 1 FROM inventarios i JOIN lotes l ON l.id = i.lote_id
                                  WHERE i.lote_id = a.lote_id AND i.stock_actual > 0
                                    AND i.deleted_at IS NULL AND l.deleted_at IS NULL)""");
        // El aviso previo queda superado cuando el lote ya venció (su alerta VENCIDO lo reemplaza)
        int superadas = jdbc.update("""
                UPDATE alertas a SET resuelta_at = CURRENT_TIMESTAMP
                FROM lotes l
                WHERE l.id = a.lote_id AND a.tipo = 'VENCIMIENTO' AND a.resuelta_at IS NULL
                  AND l.fecha_vencimiento < ?""", Date.valueOf(hoy));
        // Stock repuesto, mínimo quitado o producto dado de baja
        int stock = jdbc.update("""
                UPDATE alertas a SET resuelta_at = CURRENT_TIMESTAMP
                FROM productos p
                LEFT JOIN (%s) s ON s.producto_id = p.id
                WHERE p.id = a.producto_id AND a.tipo = 'STOCK_BAJO' AND a.resuelta_at IS NULL
                  AND (p.deleted_at IS NOT NULL OR p.estado <> 'ACTIVO' OR p.stock_minimo <= 0
                       OR COALESCE(s.stock, 0) >= p.stock_minimo)""".formatted(STOCK_POR_PRODUCTO));
        return lotes + superadas + stock;
    }
}

package com.tienda.backend.reporte.infrastructure.adapter.out.persistence;

import com.tienda.backend.reporte.application.model.FilasCompras;
import com.tienda.backend.reporte.application.model.RangoFechas;
import com.tienda.backend.reporte.application.port.out.ReporteComprasRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reportes de compras sobre pedidos y recepciones a proveedores. Los borradores no son compras; los cancelados no
 * entregan nada. Los importes usan el costo pactado en cada línea del pedido, que es el costo del lote recibido.
 */
@Component
@RequiredArgsConstructor
public class ReporteComprasJdbcAdapter implements ReporteComprasRepositoryPort {

    private final JdbcTemplate jdbc;

    @Override
    public List<FilasCompras.Proveedor> porProveedor(RangoFechas rango) {
        return jdbc.query("""
                SELECT pr.id, pr.nombre_empresa,
                       COUNT(*) FILTER (WHERE p.estado IN ('ENVIADO', 'RECIBIDO_PARCIAL', 'RECIBIDO')),
                       COUNT(*) FILTER (WHERE p.estado = 'CANCELADO'),
                       COALESCE(SUM(p.total) FILTER (WHERE p.estado IN ('ENVIADO', 'RECIBIDO_PARCIAL', 'RECIBIDO')), 0),
                       COALESCE(SUM(r.monto) FILTER (WHERE p.estado IN ('ENVIADO', 'RECIBIDO_PARCIAL', 'RECIBIDO')), 0)
                FROM pedidos_proveedor p
                JOIN proveedores pr ON pr.id = p.proveedor_id
                LEFT JOIN LATERAL (SELECT SUM(pd.cantidad_recibida * pd.costo_unitario) AS monto
                                   FROM pedido_proveedor_detalle pd WHERE pd.pedido_id = p.id) r ON TRUE
                WHERE p.deleted_at IS NULL AND p.estado <> 'BORRADOR'
                  AND p.fecha_pedido >= ? AND p.fecha_pedido <= ?
                GROUP BY pr.id, pr.nombre_empresa
                ORDER BY 5 DESC, pr.nombre_empresa
                """, (rs, n) -> {
            BigDecimal pedido = rs.getBigDecimal(5);
            BigDecimal recibido = rs.getBigDecimal(6);
            return new FilasCompras.Proveedor(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getLong(4), m(pedido),
                    m(recibido), m(pedido.subtract(recibido)),
                    pedido.signum() == 0 ? null
                            : recibido.multiply(BigDecimal.valueOf(100)).divide(pedido, 2, RoundingMode.HALF_UP));
        }, Date.valueOf(rango.desde()), Date.valueOf(rango.hasta()));
    }

    @Override
    public List<FilasCompras.Pendiente> pendientes(LocalDate hoy, Long proveedorId, Long depositoId) {
        List<Object> args = new ArrayList<>();
        StringBuilder filtros = new StringBuilder();
        if (proveedorId != null) {
            filtros.append(" AND p.proveedor_id = ?");
            args.add(proveedorId);
        }
        if (depositoId != null) {
            filtros.append(" AND p.deposito_destino_id = ?");
            args.add(depositoId);
        }
        return jdbc.query("""
                SELECT p.id, p.numero_pedido, p.estado, pr.id, pr.nombre_empresa, d.id, d.nombre, p.fecha_pedido,
                       p.fecha_entrega_estimada, p.total,
                       COUNT(*) FILTER (WHERE pd.cantidad_pedida > pd.cantidad_recibida),
                       COALESCE(SUM(pd.cantidad_pedida - pd.cantidad_recibida), 0),
                       COALESCE(SUM((pd.cantidad_pedida - pd.cantidad_recibida) * pd.costo_unitario), 0)
                FROM pedidos_proveedor p
                JOIN proveedores pr ON pr.id = p.proveedor_id
                JOIN depositos d ON d.id = p.deposito_destino_id
                JOIN pedido_proveedor_detalle pd ON pd.pedido_id = p.id
                WHERE p.deleted_at IS NULL AND p.estado IN ('ENVIADO', 'RECIBIDO_PARCIAL')%s
                GROUP BY p.id, p.numero_pedido, p.estado, pr.id, pr.nombre_empresa, d.id, d.nombre, p.fecha_pedido,
                         p.fecha_entrega_estimada, p.total
                ORDER BY p.fecha_entrega_estimada ASC NULLS LAST, p.fecha_pedido, p.id
                """.formatted(filtros), (rs, n) -> {
            LocalDate estimada = rs.getObject(9, LocalDate.class);
            Long atraso = estimada == null ? null : Math.max(0, ChronoUnit.DAYS.between(estimada, hoy));
            BigDecimal unidades = rs.getBigDecimal(12);
            return new FilasCompras.Pendiente(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getLong(4),
                    rs.getString(5), rs.getLong(6), rs.getString(7), rs.getObject(8, LocalDate.class), estimada, atraso,
                    m(rs.getBigDecimal(10)), rs.getLong(11), cant(unidades), m(rs.getBigDecimal(13)));
        }, args.toArray());
    }

    @Override
    public Optional<String> nombreProducto(Long productoId) {
        return jdbc.query("SELECT nombre FROM productos WHERE id = ? AND deleted_at IS NULL",
                (rs, n) -> rs.getString(1), productoId).stream().findFirst();
    }

    @Override
    public List<FilasCompras.CostoProducto> comprasDeProducto(Long productoId, Long proveedorId, RangoFechas rango) {
        List<Object> args = new ArrayList<>(List.of(productoId, Timestamp.valueOf(rango.inicio()),
                Timestamp.valueOf(rango.finExclusivo())));
        String filtroProveedor = "";
        if (proveedorId != null) {
            filtroProveedor = " AND p.proveedor_id = ?";
            args.add(proveedorId);
        }
        return jdbc.query("""
                SELECT r.fecha_recepcion, p.numero_pedido, pr.id, pr.nombre_empresa, l.numero_lote, rd.cantidad,
                       pd.costo_unitario
                FROM recepcion_pedido_detalle rd
                JOIN recepciones_pedido r ON r.id = rd.recepcion_id
                JOIN pedido_proveedor_detalle pd ON pd.id = rd.pedido_detalle_id
                JOIN pedidos_proveedor p ON p.id = pd.pedido_id
                JOIN proveedores pr ON pr.id = p.proveedor_id
                JOIN lotes l ON l.id = rd.lote_id
                WHERE pd.producto_id = ? AND r.fecha_recepcion >= ? AND r.fecha_recepcion < ?%s
                ORDER BY r.fecha_recepcion, rd.id
                """.formatted(filtroProveedor), (rs, n) -> new FilasCompras.CostoProducto(
                rs.getTimestamp(1).toLocalDateTime(), rs.getString(2), rs.getLong(3), rs.getString(4), rs.getString(5),
                cant(rs.getBigDecimal(6)), costo(rs.getBigDecimal(7)), null), args.toArray());
    }

    // ------------------------------------------------------------------ utilidades

    private static BigDecimal m(BigDecimal valor) {
        return (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_UP);
    }

    /** Cantidad en unidad base: hasta 4 decimales, sin ceros de relleno. */
    private static BigDecimal cant(BigDecimal valor) {
        BigDecimal v = (valor == null ? BigDecimal.ZERO : valor).stripTrailingZeros();
        return v.scale() < 0 ? v.setScale(0) : v;
    }

    /** Costo unitario: hasta 4 decimales, con al menos 2. */
    private static BigDecimal costo(BigDecimal valor) {
        BigDecimal v = valor.stripTrailingZeros();
        return v.scale() < 2 ? v.setScale(2) : v;
    }
}

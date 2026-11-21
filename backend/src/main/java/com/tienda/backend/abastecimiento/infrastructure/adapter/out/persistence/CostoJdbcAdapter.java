package com.tienda.backend.abastecimiento.infrastructure.adapter.out.persistence;

import com.tienda.backend.abastecimiento.application.model.CostoFiltro;
import com.tienda.backend.abastecimiento.application.model.HistoricoCosto;
import com.tienda.backend.abastecimiento.application.port.out.CostoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Lee la vista v_historico_costos_proveedor (líneas de pedidos confirmados). */
@Component
@RequiredArgsConstructor
public class CostoJdbcAdapter implements CostoRepositoryPort {

    private final JdbcTemplate jdbc;

    @Override
    public List<HistoricoCosto> historial(CostoFiltro filtro, LocalDate desde) {
        StringBuilder sql = new StringBuilder("""
                SELECT h.detalle_id, h.pedido_id, h.numero_pedido, h.estado, h.proveedor_id,
                       prov.nombre_empresa AS proveedor, h.producto_id, p.nombre AS producto, c.nombre AS categoria,
                       h.fecha, h.costo_unitario, h.cantidad_pedida
                FROM v_historico_costos_proveedor h
                JOIN proveedores prov ON prov.id = h.proveedor_id
                JOIN productos p ON p.id = h.producto_id
                JOIN categorias c ON c.id = p.categoria_id
                WHERE h.fecha >= ? AND p.deleted_at IS NULL""");
        List<Object> args = new ArrayList<>();
        args.add(Date.valueOf(desde));
        if (filtro.proveedorId() != null) {
            sql.append(" AND h.proveedor_id = ?");
            args.add(filtro.proveedorId());
        }
        if (filtro.productoId() != null) {
            sql.append(" AND h.producto_id = ?");
            args.add(filtro.productoId());
        }
        if (filtro.search() != null && !filtro.search().isBlank()) {
            sql.append(" AND (LOWER(p.nombre) LIKE ? ESCAPE '\\' OR LOWER(prov.nombre_empresa) LIKE ? ESCAPE '\\')");
            String patron = "%" + escaparLike(filtro.search().trim().toLowerCase(Locale.ROOT)) + "%";
            args.add(patron);
            args.add(patron);
        }
        sql.append(" ORDER BY h.proveedor_id, h.producto_id, h.fecha, h.detalle_id");
        return jdbc.query(sql.toString(), (rs, i) -> new HistoricoCosto(rs.getLong("detalle_id"), rs.getLong("pedido_id"),
                rs.getString("numero_pedido"), rs.getString("estado"), rs.getLong("proveedor_id"),
                rs.getString("proveedor"), rs.getLong("producto_id"), rs.getString("producto"),
                rs.getString("categoria"), rs.getObject("fecha", LocalDate.class),
                limpio(rs.getBigDecimal("costo_unitario")),
                limpio(rs.getBigDecimal("cantidad_pedida")), null), args.toArray());
    }

    /** 12.5000 -> 12.5 y 120.0000 -> 120 (sin notación científica 1.2E+2). */
    private static BigDecimal limpio(BigDecimal v) {
        BigDecimal l = v.stripTrailingZeros();
        return l.scale() < 0 ? l.setScale(0) : l;
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

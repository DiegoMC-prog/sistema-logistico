package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import com.tienda.backend.precio.application.model.RecargoAplicado;
import com.tienda.backend.precio.application.model.RecargoFiltro;
import com.tienda.backend.precio.application.model.RecargoResumen;
import com.tienda.backend.precio.application.port.out.RecargoRepositoryPort;
import com.tienda.backend.precio.domain.exception.RecargoConflictoException;
import com.tienda.backend.precio.domain.model.AlcanceRecargo;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Persistencia de recargos por escasez (tabla recargos_escasez, migración V10). */
@Component
@RequiredArgsConstructor
public class RecargoJdbcAdapter implements RecargoRepositoryPort {

    private final JdbcTemplate jdbc;

    private static final String SELECT = """
            SELECT r.*, p.nombre AS producto, c.nombre AS categoria
            FROM recargos_escasez r
            LEFT JOIN productos p ON p.id = r.producto_id
            LEFT JOIN categorias c ON c.id = r.categoria_id
            """;

    private static final RowMapper<RecargoResumen> RECARGO = (rs, i) -> new RecargoResumen(rs.getLong("id"),
            lng(rs, "producto_id") != null ? AlcanceRecargo.PRODUCTO : AlcanceRecargo.CATEGORIA,
            lng(rs, "producto_id"), rs.getString("producto"), lng(rs, "categoria_id"), rs.getString("categoria"),
            limpio(rs.getBigDecimal("porcentaje")), rs.getString("motivo"), rs.getBoolean("activo"),
            rs.getString("activado_por"), fecha(rs, "activado_at"), rs.getString("desactivado_por"),
            fecha(rs, "desactivado_at"));

    @Override
    public PageResult<RecargoResumen> listar(RecargoFiltro filtro, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        List<Object> args = new ArrayList<>();
        if (filtro.activo() != null) {
            where.append(" AND r.activo = ?");
            args.add(filtro.activo());
        }
        if (filtro.alcance() == AlcanceRecargo.PRODUCTO) {
            where.append(" AND r.producto_id IS NOT NULL");
        } else if (filtro.alcance() == AlcanceRecargo.CATEGORIA) {
            where.append(" AND r.categoria_id IS NOT NULL");
        }
        if (filtro.search() != null && !filtro.search().isBlank()) {
            where.append(" AND (LOWER(p.nombre) LIKE ? ESCAPE '\\' OR LOWER(c.nombre) LIKE ? ESCAPE '\\'"
                         + " OR LOWER(r.motivo) LIKE ? ESCAPE '\\')");
            String patron = "%" + escaparLike(filtro.search().trim().toLowerCase(Locale.ROOT)) + "%";
            args.add(patron);
            args.add(patron);
            args.add(patron);
        }
        Long total = jdbc.queryForObject("""
                SELECT COUNT(*) FROM recargos_escasez r
                LEFT JOIN productos p ON p.id = r.producto_id
                LEFT JOIN categorias c ON c.id = r.categoria_id""" + where, Long.class, args.toArray());
        long totalElements = total == null ? 0 : total;

        List<Object> argsPagina = new ArrayList<>(args);
        argsPagina.add(size);
        argsPagina.add((long) page * size);
        // Activos primero; dentro de cada grupo, lo más reciente arriba
        List<RecargoResumen> contenido = jdbc.query(SELECT + where
                + " ORDER BY r.activo DESC, COALESCE(r.desactivado_at, r.activado_at) DESC, r.id DESC LIMIT ? OFFSET ?",
                RECARGO, argsPagina.toArray());
        int totalPages = (int) ((totalElements + size - 1) / size);
        return new PageResult<>(contenido, page, size, totalElements, totalPages);
    }

    @Override
    public Optional<RecargoResumen> buscar(Long id) {
        return jdbc.query(SELECT + " WHERE r.id = ?", RECARGO, id).stream().findFirst();
    }

    @Override
    public Long crear(AlcanceRecargo alcance, Long destinoId, BigDecimal porcentaje, String motivo, String usuario) {
        KeyHolder key = new GeneratedKeyHolder();
        try {
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement("""
                        INSERT INTO recargos_escasez (producto_id, categoria_id, porcentaje, motivo, activado_por)
                        VALUES (?, ?, ?, ?, ?)""", new String[]{"id"});
                if (alcance == AlcanceRecargo.PRODUCTO) {
                    ps.setLong(1, destinoId);
                    ps.setNull(2, Types.BIGINT);
                } else {
                    ps.setNull(1, Types.BIGINT);
                    ps.setLong(2, destinoId);
                }
                ps.setBigDecimal(3, porcentaje);
                ps.setString(4, motivo);
                ps.setString(5, usuario);
                return ps;
            }, key);
        } catch (DuplicateKeyException e) {
            throw new RecargoConflictoException("Ya hay un recargo activo para ese destino. Desactívelo antes de activar otro.");
        }
        return key.getKey().longValue();
    }

    @Override
    public boolean desactivar(Long id, String usuario) {
        return jdbc.update("""
                UPDATE recargos_escasez SET activo = FALSE, desactivado_por = ?, desactivado_at = CURRENT_TIMESTAMP
                WHERE id = ? AND activo""", usuario, id) > 0;
    }

    @Override
    public Optional<RecargoResumen> activo(AlcanceRecargo alcance, Long destinoId) {
        String columna = alcance == AlcanceRecargo.PRODUCTO ? "r.producto_id" : "r.categoria_id";
        return jdbc.query(SELECT + " WHERE r.activo AND " + columna + " = ?", RECARGO, destinoId).stream().findFirst();
    }

    @Override
    public Optional<String> nombreDestino(AlcanceRecargo alcance, Long destinoId) {
        String tabla = alcance == AlcanceRecargo.PRODUCTO ? "productos" : "categorias";
        return jdbc.query("SELECT nombre FROM " + tabla + " WHERE id = ? AND deleted_at IS NULL",
                (rs, i) -> rs.getString(1), destinoId).stream().findFirst();
    }

    @Override
    public Optional<RecargoAplicado> vigentePorPresentacion(Long presentacionId) {
        // El recargo del producto gana sobre el de su categoría
        return jdbc.query("""
                SELECT r.id, r.producto_id, r.porcentaje, r.motivo
                FROM presentaciones_producto pr
                JOIN productos p ON p.id = pr.producto_id
                JOIN recargos_escasez r ON r.activo AND (r.producto_id = p.id OR r.categoria_id = p.categoria_id)
                WHERE pr.id = ?
                ORDER BY CASE WHEN r.producto_id IS NOT NULL THEN 0 ELSE 1 END
                LIMIT 1""", (rs, i) -> new RecargoAplicado(rs.getLong("id"),
                lng(rs, "producto_id") != null ? AlcanceRecargo.PRODUCTO : AlcanceRecargo.CATEGORIA,
                limpio(rs.getBigDecimal("porcentaje")), rs.getString("motivo")), presentacionId).stream().findFirst();
    }

    private static Long lng(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    private static LocalDateTime fecha(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    private static BigDecimal limpio(BigDecimal v) {
        BigDecimal l = v.stripTrailingZeros();
        return l.scale() < 0 ? l.setScale(0) : l;
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

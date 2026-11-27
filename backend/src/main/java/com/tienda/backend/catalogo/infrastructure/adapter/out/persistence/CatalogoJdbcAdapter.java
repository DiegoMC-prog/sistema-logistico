package com.tienda.backend.catalogo.infrastructure.adapter.out.persistence;

import com.tienda.backend.catalogo.application.model.CatalogoFiltro;
import com.tienda.backend.catalogo.application.model.CatalogoPresentacion;
import com.tienda.backend.catalogo.application.model.CatalogoProducto;
import com.tienda.backend.catalogo.application.port.out.CatalogoRepositoryPort;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CatalogoJdbcAdapter implements CatalogoRepositoryPort {

    private final JdbcTemplate jdbc;

    @Override
    public PageResult<CatalogoProducto> listar(CatalogoFiltro filtro, int page, int size) {
        boolean conStock = filtro.depositoId() != null;
        List<Object> args = new ArrayList<>();

        StringBuilder from = new StringBuilder(" FROM productos p JOIN categorias c ON c.id = p.categoria_id");
        if (conStock) {
            from.append(" LEFT JOIN (SELECT l.producto_id, SUM(i.stock_actual) AS stock")
                    .append(" FROM inventarios i JOIN lotes l ON l.id = i.lote_id")
                    .append(" WHERE i.deleted_at IS NULL AND l.deleted_at IS NULL AND i.deposito_id = ?");
            args.add(filtro.depositoId());
            if (!filtro.incluirVencidos()) {
                from.append(" AND (l.fecha_vencimiento IS NULL OR l.fecha_vencimiento >= ?)");
                args.add(Date.valueOf(LocalDate.now()));
            }
            from.append(" GROUP BY l.producto_id) s ON s.producto_id = p.id");
        }

        from.append(" WHERE p.deleted_at IS NULL AND p.estado = 'ACTIVO'");
        if (filtro.categoriaId() != null) {
            from.append(" AND p.categoria_id = ?");
            args.add(filtro.categoriaId());
        }
        if (filtro.search() != null && !filtro.search().isBlank()) {
            from.append(" AND LOWER(p.nombre) LIKE ? ESCAPE '\\'");
            args.add("%" + escaparLike(filtro.search().trim().toLowerCase(Locale.ROOT)) + "%");
        }
        if (filtro.soloConStock() && conStock) {
            from.append(" AND COALESCE(s.stock, 0) > 0");
        }

        Long total = jdbc.queryForObject("SELECT COUNT(*)" + from, Long.class, args.toArray());
        long totalElements = total == null ? 0 : total;

        List<Object> argsPagina = new ArrayList<>(args);
        argsPagina.add(size);
        argsPagina.add((long) page * size);
        String columnas = "SELECT p.id, p.nombre, c.nombre AS categoria, "
                          + (conStock ? "COALESCE(s.stock, 0)" : "CAST(NULL AS NUMERIC)") + " AS stock";
        List<CatalogoProducto> productos = jdbc.query(columnas + from + " ORDER BY p.nombre, p.id LIMIT ? OFFSET ?",
                (rs, i) -> new CatalogoProducto(rs.getLong("id"), rs.getString("nombre"), rs.getString("categoria"),
                        rs.getBigDecimal("stock"), null, List.of()),
                argsPagina.toArray());

        int totalPages = (int) ((totalElements + size - 1) / size);
        return new PageResult<>(conPresentaciones(productos), page, size, totalElements, totalPages);
    }

    /** Agrega a cada producto sus presentaciones (de menor a mayor factor) y la abreviatura de su unidad base. */
    private List<CatalogoProducto> conPresentaciones(List<CatalogoProducto> productos) {
        if (productos.isEmpty()) {
            return productos;
        }
        Map<Long, List<CatalogoPresentacion>> presentaciones = new HashMap<>();
        Map<Long, String> unidadBase = new HashMap<>();
        String marcadores = String.join(", ", Collections.nCopies(productos.size(), "?"));
        jdbc.query("SELECT pp.producto_id, pp.id, u.abreviatura, pp.factor_conversion, pp.es_unidad_base"
                   + " FROM presentaciones_producto pp JOIN unidades_medida u ON u.id = pp.unidad_medida_id"
                   + " WHERE pp.deleted_at IS NULL AND pp.producto_id IN (" + marcadores + ")"
                   + " ORDER BY pp.factor_conversion, pp.id",
                rs -> {
                    long productoId = rs.getLong("producto_id");
                    String abreviatura = rs.getString("abreviatura");
                    presentaciones.computeIfAbsent(productoId, k -> new ArrayList<>()).add(new CatalogoPresentacion(
                            rs.getLong("id"), abreviatura, rs.getBigDecimal("factor_conversion"), null, null));
                    if (rs.getBoolean("es_unidad_base")) {
                        unidadBase.put(productoId, abreviatura);
                    }
                },
                productos.stream().map(CatalogoProducto::productoId).toArray());

        return productos.stream()
                .map(p -> new CatalogoProducto(p.productoId(), p.nombre(), p.categoria(), p.stock(),
                        unidadBase.get(p.productoId()), presentaciones.getOrDefault(p.productoId(), List.of())))
                .toList();
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

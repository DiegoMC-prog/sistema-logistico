package com.tienda.backend.abastecimiento.infrastructure.adapter.out.persistence;

import com.tienda.backend.abastecimiento.application.model.CalculoResumen;
import com.tienda.backend.abastecimiento.application.model.CompraReciente;
import com.tienda.backend.abastecimiento.application.model.EscenarioResumen;
import com.tienda.backend.abastecimiento.application.model.GuardarParametrosComando;
import com.tienda.backend.abastecimiento.application.model.OrigenCalculo;
import com.tienda.backend.abastecimiento.application.model.ParametrosProducto;
import com.tienda.backend.abastecimiento.application.model.ProductoReorden;
import com.tienda.backend.abastecimiento.application.model.SugerenciaFiltro;
import com.tienda.backend.abastecimiento.application.model.SugerenciaParaPedido;
import com.tienda.backend.abastecimiento.application.model.SugerenciaResumen;
import com.tienda.backend.abastecimiento.application.port.out.AbastecimientoRepositoryPort;
import com.tienda.backend.abastecimiento.domain.model.AccionSugerencia;
import com.tienda.backend.abastecimiento.domain.model.EstadoSugerencia;
import com.tienda.backend.abastecimiento.domain.model.ResultadoEscenario;
import com.tienda.backend.abastecimiento.domain.model.SugerenciaCalculada;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Persistencia del motor de reorden con SQL directo: consultas de agregación por lote y escritura masiva. */
@Component
@RequiredArgsConstructor
public class AbastecimientoJdbcAdapter implements AbastecimientoRepositoryPort {

    private final JdbcTemplate jdbc;

    // ================================================================== datos de entrada

    @Override
    public List<ProductoReorden> productosEvaluables() {
        return jdbc.query("""
                SELECT p.id, p.nombre, p.es_perecedero, pr.costo_pedido, pr.costo_almacen_pct, pr.nivel_servicio,
                       pr.proveedor_id
                FROM productos p
                LEFT JOIN parametros_reorden pr ON pr.producto_id = p.id
                WHERE p.deleted_at IS NULL AND p.estado = 'ACTIVO' AND COALESCE(pr.activo, TRUE)
                ORDER BY p.id""", (rs, i) -> new ProductoReorden(rs.getLong("id"), rs.getString("nombre"),
                rs.getBoolean("es_perecedero"), dbl(rs, "costo_pedido"), dbl(rs, "costo_almacen_pct"),
                dbl(rs, "nivel_servicio"), lng(rs, "proveedor_id")));
    }

    @Override
    public Map<Long, Map<LocalDate, Double>> ventasDiarias(LocalDate desde, LocalDate hasta) {
        Map<Long, Map<LocalDate, Double>> out = new HashMap<>();
        jdbc.query("""
                SELECT d.producto_id, CAST(v.fecha_venta AS DATE) AS dia, SUM(d.cantidad * d.factor_conversion) AS unidades
                FROM venta_detalle d
                JOIN ventas v ON v.id = d.venta_id
                WHERE v.estado = 'COMPLETADA' AND v.fecha_venta >= ? AND v.fecha_venta < ?
                GROUP BY d.producto_id, CAST(v.fecha_venta AS DATE)""", rs -> {
            out.computeIfAbsent(rs.getLong("producto_id"), k -> new HashMap<>())
                    .put(rs.getObject("dia", LocalDate.class), rs.getDouble("unidades"));
        }, Timestamp.valueOf(desde.atStartOfDay()), Timestamp.valueOf(hasta.atStartOfDay()));
        return out;
    }

    @Override
    public Map<Long, LocalDate> primeraVenta() {
        Map<Long, LocalDate> out = new HashMap<>();
        jdbc.query("""
                SELECT d.producto_id, CAST(MIN(v.fecha_venta) AS DATE) AS primera
                FROM venta_detalle d JOIN ventas v ON v.id = d.venta_id
                WHERE v.estado = 'COMPLETADA'
                GROUP BY d.producto_id""",
                rs -> { out.put(rs.getLong(1), rs.getObject(2, LocalDate.class)); });
        return out;
    }

    @Override
    public List<CompraReciente> comprasRecientes() {
        return jdbc.query("""
                SELECT DISTINCT ON (d.producto_id, pp.proveedor_id)
                       d.producto_id, pp.proveedor_id, d.costo_unitario, pp.fecha_pedido
                FROM pedido_proveedor_detalle d
                JOIN pedidos_proveedor pp ON pp.id = d.pedido_id
                WHERE pp.deleted_at IS NULL AND pp.estado <> 'CANCELADO'
                ORDER BY d.producto_id, pp.proveedor_id, pp.fecha_pedido DESC, pp.id DESC""",
                (rs, i) -> new CompraReciente(rs.getLong(1), rs.getLong(2), rs.getDouble(3),
                        rs.getObject(4, LocalDate.class)));
    }

    @Override
    public Map<Long, Double> costoUltimoLote() {
        return mapa("""
                SELECT DISTINCT ON (producto_id) producto_id, costo_unitario_compra
                FROM lotes WHERE deleted_at IS NULL AND costo_unitario_compra > 0
                ORDER BY producto_id, created_at DESC, id DESC""");
    }

    @Override
    public Map<Long, List<Double>> leadTimesPorProveedor(LocalDate desde) {
        Map<Long, List<Double>> out = new HashMap<>();
        jdbc.query("""
                SELECT pp.proveedor_id, GREATEST(CAST(MIN(r.fecha_recepcion) AS DATE) - pp.fecha_pedido, 0) AS dias
                FROM pedidos_proveedor pp
                JOIN recepciones_pedido r ON r.pedido_id = pp.id
                WHERE pp.deleted_at IS NULL AND pp.fecha_pedido >= ?
                GROUP BY pp.id, pp.proveedor_id, pp.fecha_pedido""", rs -> {
            out.computeIfAbsent(rs.getLong(1), k -> new ArrayList<>()).add(rs.getDouble(2));
        }, Date.valueOf(desde));
        return out;
    }

    @Override
    public Map<Long, Double> stockVendible(LocalDate hoy) {
        return mapa("""
                SELECT l.producto_id, SUM(i.stock_actual)
                FROM inventarios i JOIN lotes l ON l.id = i.lote_id
                WHERE i.deleted_at IS NULL AND l.deleted_at IS NULL
                  AND (l.fecha_vencimiento IS NULL OR l.fecha_vencimiento >= ?)
                GROUP BY l.producto_id""", Date.valueOf(hoy));
    }

    @Override
    public Map<Long, Double> enTransito() {
        return mapa("""
                SELECT d.producto_id, SUM(d.cantidad_pedida - d.cantidad_recibida)
                FROM pedido_proveedor_detalle d
                JOIN pedidos_proveedor pp ON pp.id = d.pedido_id
                WHERE pp.deleted_at IS NULL AND pp.estado IN ('BORRADOR', 'ENVIADO', 'RECIBIDO_PARCIAL')
                GROUP BY d.producto_id""");
    }

    @Override
    public Map<Long, Double> vidaUtilMedia(LocalDate desde) {
        return mapa("""
                SELECT producto_id, AVG(fecha_vencimiento - CAST(created_at AS DATE))
                FROM lotes
                WHERE deleted_at IS NULL AND fecha_vencimiento IS NOT NULL AND created_at >= ?
                  AND fecha_vencimiento > CAST(created_at AS DATE)
                GROUP BY producto_id""", Timestamp.valueOf(desde.atStartOfDay()));
    }

    private Map<Long, Double> mapa(String sql, Object... args) {
        Map<Long, Double> out = new HashMap<>();
        jdbc.query(sql, rs -> { out.put(rs.getLong(1), rs.getDouble(2)); }, args);
        return out;
    }

    // ================================================================== cálculos

    private static final String INSERT_SUGERENCIA = """
            INSERT INTO sugerencias_reorden (calculo_id, producto_id, proveedor_id, estado, dias_historial,
                demanda_diaria, desviacion_demanda, lead_time_dias, desviacion_lead_time, muestras_lead_time,
                costo_unitario, costo_pedido, costo_almacen_anual, nivel_servicio, factor_z, stock_actual, en_transito,
                vida_util_dias,
                eoq_pesimista, stock_seguridad_pesimista, punto_reorden_pesimista, cantidad_pesimista,
                eoq_estandar, stock_seguridad_estandar, punto_reorden_estandar, cantidad_estandar,
                eoq_optimista, stock_seguridad_optimista, punto_reorden_optimista, cantidad_optimista,
                motivo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""";

    @Override
    public Long guardarCalculo(OrigenCalculo origen, String usuario, int diasVentana, int historialMinimoDias,
                               List<SugerenciaCalculada> sugerencias) {
        int aReordenar = (int) sugerencias.stream().filter(s -> s.estado() == EstadoSugerencia.REORDENAR).count();
        KeyHolder key = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO calculos_reorden (origen, ejecutado_por, dias_ventana, historial_minimo_dias,
                                                  productos_evaluados, productos_a_reordenar)
                    VALUES (?, ?, ?, ?, ?, ?)""", new String[]{"id"});
            ps.setString(1, origen.name());
            ps.setString(2, usuario);
            ps.setInt(3, diasVentana);
            ps.setInt(4, historialMinimoDias);
            ps.setInt(5, sugerencias.size());
            ps.setInt(6, aReordenar);
            return ps;
        }, key);
        Long calculoId = key.getKey().longValue();

        jdbc.batchUpdate(INSERT_SUGERENCIA, sugerencias, 200, (ps, s) -> {
            int c = 1;
            ps.setLong(c++, calculoId);
            ps.setLong(c++, s.productoId());
            if (s.proveedorId() == null) ps.setNull(c++, Types.BIGINT); else ps.setLong(c++, s.proveedorId());
            ps.setString(c++, s.estado().name());
            ps.setInt(c++, s.diasHistorial());
            ps.setBigDecimal(c++, bd(s.demanda().media()));
            ps.setBigDecimal(c++, bd(s.demanda().desviacion()));
            ps.setBigDecimal(c++, bd(s.leadTime().media(), 2));
            ps.setBigDecimal(c++, bd(s.leadTime().desviacion(), 2));
            ps.setInt(c++, s.leadTime().muestras());
            ps.setBigDecimal(c++, bd(s.costoUnitario()));
            ps.setBigDecimal(c++, bd(s.costoPedido()));
            ps.setBigDecimal(c++, bd(s.costoAlmacenAnual()));
            ps.setBigDecimal(c++, bd(s.nivelServicio()));
            ps.setBigDecimal(c++, bd(s.factorZ(), 3));
            ps.setBigDecimal(c++, bd(s.stockActual()));
            ps.setBigDecimal(c++, bd(s.enTransito()));
            if (s.vidaUtilDias() == null) ps.setNull(c++, Types.DECIMAL); else ps.setBigDecimal(c++, bd(s.vidaUtilDias(), 2));
            for (ResultadoEscenario e : List.of(s.pesimista(), s.estandar(), s.optimista())) {
                ps.setBigDecimal(c++, bd(e.eoq()));
                ps.setBigDecimal(c++, bd(e.stockSeguridad()));
                ps.setBigDecimal(c++, bd(e.puntoReorden()));
                ps.setBigDecimal(c++, bd(e.cantidad()));
            }
            String motivo = s.motivo();
            ps.setString(c, motivo.length() > 1000 ? motivo.substring(0, 1000) : motivo);
        });
        return calculoId;
    }

    @Override
    public boolean existeCalculo() {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM calculos_reorden)", Boolean.class));
    }

    private static final String SELECT_CALCULO = """
            SELECT id, fecha, origen, ejecutado_por, dias_ventana, historial_minimo_dias, productos_evaluados,
                   productos_a_reordenar
            FROM calculos_reorden""";

    @Override
    public Optional<CalculoResumen> ultimoCalculo() {
        return jdbc.query(SELECT_CALCULO + " ORDER BY id DESC LIMIT 1", calculoMapper()).stream().findFirst();
    }

    @Override
    public Optional<CalculoResumen> calculo(Long id) {
        return jdbc.query(SELECT_CALCULO + " WHERE id = ?", calculoMapper(), id).stream().findFirst();
    }

    private RowMapper<CalculoResumen> calculoMapper() {
        return this::mapearCalculo;
    }

    private CalculoResumen mapearCalculo(ResultSet rs, int i) throws SQLException {
        long id = rs.getLong("id");
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoSugerencia e : EstadoSugerencia.values()) {
            porEstado.put(e.name(), 0L);
        }
        jdbc.query("SELECT estado, COUNT(*) FROM sugerencias_reorden WHERE calculo_id = ? GROUP BY estado",
                r -> { porEstado.put(r.getString(1), r.getLong(2)); }, id);
        return new CalculoResumen(id, rs.getTimestamp("fecha").toLocalDateTime(),
                OrigenCalculo.valueOf(rs.getString("origen")), rs.getString("ejecutado_por"),
                rs.getInt("dias_ventana"), rs.getInt("historial_minimo_dias"), rs.getInt("productos_evaluados"),
                rs.getInt("productos_a_reordenar"), porEstado);
    }

    // ================================================================== sugerencias

    private static final String SELECT_SUGERENCIA = """
            SELECT s.*, p.nombre AS producto, c.nombre AS categoria, p.es_perecedero,
                   prov.nombre_empresa AS proveedor, pp.numero_pedido
            FROM sugerencias_reorden s
            JOIN productos p ON p.id = s.producto_id
            JOIN categorias c ON c.id = p.categoria_id
            LEFT JOIN proveedores prov ON prov.id = s.proveedor_id
            LEFT JOIN pedidos_proveedor pp ON pp.id = s.pedido_id
            """;

    private static final RowMapper<SugerenciaResumen> SUGERENCIA = (rs, i) -> {
        Timestamp accionAt = rs.getTimestamp("accion_at");
        return new SugerenciaResumen(rs.getLong("id"), rs.getLong("calculo_id"), rs.getLong("producto_id"),
                rs.getString("producto"), rs.getString("categoria"), rs.getBoolean("es_perecedero"),
                lng(rs, "proveedor_id"), rs.getString("proveedor"), EstadoSugerencia.valueOf(rs.getString("estado")),
                AccionSugerencia.valueOf(rs.getString("accion")), lng(rs, "pedido_id"), rs.getString("numero_pedido"),
                rs.getString("accion_por"), accionAt == null ? null : accionAt.toLocalDateTime(),
                rs.getInt("dias_historial"), num(rs, "demanda_diaria"), num(rs, "desviacion_demanda"),
                num(rs, "lead_time_dias"), num(rs, "desviacion_lead_time"), rs.getInt("muestras_lead_time"),
                num(rs, "costo_unitario"), num(rs, "costo_pedido"), num(rs, "costo_almacen_anual"),
                num(rs, "nivel_servicio"), num(rs, "factor_z"), num(rs, "stock_actual"), num(rs, "en_transito"),
                num(rs, "vida_util_dias"), escenario(rs, "pesimista"), escenario(rs, "estandar"),
                escenario(rs, "optimista"), rs.getString("motivo"));
    };

    private static EscenarioResumen escenario(ResultSet rs, String sufijo) throws SQLException {
        return new EscenarioResumen(num(rs, "eoq_" + sufijo), num(rs, "stock_seguridad_" + sufijo),
                num(rs, "punto_reorden_" + sufijo), num(rs, "cantidad_" + sufijo));
    }

    @Override
    public PageResult<SugerenciaResumen> listar(Long calculoId, SugerenciaFiltro filtro, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE s.calculo_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(calculoId);
        if (filtro.estado() != null) {
            where.append(" AND s.estado = ?");
            args.add(filtro.estado().name());
        }
        if (filtro.accion() != null) {
            where.append(" AND s.accion = ?");
            args.add(filtro.accion().name());
        }
        if (filtro.proveedorId() != null) {
            where.append(" AND s.proveedor_id = ?");
            args.add(filtro.proveedorId());
        }
        if (filtro.search() != null && !filtro.search().isBlank()) {
            where.append(" AND LOWER(p.nombre) LIKE ? ESCAPE '\\'");
            args.add("%" + escaparLike(filtro.search().trim().toLowerCase(Locale.ROOT)) + "%");
        }

        Long total = jdbc.queryForObject("SELECT COUNT(*) FROM sugerencias_reorden s JOIN productos p ON p.id = s.producto_id"
                + where, Long.class, args.toArray());
        long totalElements = total == null ? 0 : total;

        List<Object> argsPagina = new ArrayList<>(args);
        argsPagina.add(size);
        argsPagina.add((long) page * size);
        // Primero lo que hay que pedir (y con mayor faltante respecto del punto de reorden), luego el resto
        List<SugerenciaResumen> contenido = jdbc.query(SELECT_SUGERENCIA + where + """
                 ORDER BY CASE s.estado WHEN 'REORDENAR' THEN 0 WHEN 'SIN_NECESIDAD' THEN 1 ELSE 2 END,
                          CASE s.accion WHEN 'PENDIENTE' THEN 0 ELSE 1 END,
                          (s.stock_actual + s.en_transito) - s.punto_reorden_estandar,
                          p.nombre
                 LIMIT ? OFFSET ?""", SUGERENCIA, argsPagina.toArray());
        int totalPages = (int) ((totalElements + size - 1) / size);
        return new PageResult<>(contenido, page, size, totalElements, totalPages);
    }

    @Override
    public Optional<SugerenciaResumen> sugerencia(Long id) {
        return jdbc.query(SELECT_SUGERENCIA + " WHERE s.id = ?", SUGERENCIA, id).stream().findFirst();
    }

    @Override
    public List<SugerenciaParaPedido> bloquearParaPedido(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        String marcadores = String.join(", ", java.util.Collections.nCopies(ids.size(), "?"));
        return jdbc.query("""
                SELECT s.id, s.producto_id, p.nombre, s.proveedor_id, s.accion, s.cantidad_pesimista,
                       s.cantidad_estandar, s.cantidad_optimista, s.costo_unitario
                FROM sugerencias_reorden s JOIN productos p ON p.id = s.producto_id
                WHERE s.id IN (%s)
                FOR UPDATE OF s""".formatted(marcadores),
                (rs, i) -> new SugerenciaParaPedido(rs.getLong(1), rs.getLong(2), rs.getString(3), lng(rs, "proveedor_id"),
                        AccionSugerencia.valueOf(rs.getString(5)), rs.getBigDecimal(6), rs.getBigDecimal(7),
                        rs.getBigDecimal(8), rs.getBigDecimal(9)),
                ids.toArray());
    }

    @Override
    public void marcarPedidoCreado(List<Long> sugerenciaIds, Long pedidoId, String usuario) {
        jdbc.batchUpdate("""
                UPDATE sugerencias_reorden SET accion = 'PEDIDO_CREADO', pedido_id = ?, accion_por = ?,
                       accion_at = CURRENT_TIMESTAMP
                WHERE id = ? AND accion = 'PENDIENTE'""", sugerenciaIds, sugerenciaIds.size(), (ps, id) -> {
            ps.setLong(1, pedidoId);
            ps.setString(2, usuario);
            ps.setLong(3, id);
        });
    }

    @Override
    public boolean descartar(Long id, String usuario) {
        return jdbc.update("""
                UPDATE sugerencias_reorden SET accion = 'DESCARTADA', accion_por = ?, accion_at = CURRENT_TIMESTAMP
                WHERE id = ? AND accion = 'PENDIENTE'""", usuario, id) > 0;
    }

    // ================================================================== parámetros

    @Override
    public Optional<ParametrosProducto> parametros(Long productoId, double costoPedido, double costoAlmacenPct,
                                                  double nivelServicio) {
        return jdbc.query("""
                SELECT p.id, p.nombre, pr.costo_pedido, pr.costo_almacen_pct, pr.nivel_servicio, pr.proveedor_id,
                       prov.nombre_empresa, COALESCE(pr.activo, TRUE) AS activo
                FROM productos p
                LEFT JOIN parametros_reorden pr ON pr.producto_id = p.id
                LEFT JOIN proveedores prov ON prov.id = pr.proveedor_id
                WHERE p.id = ? AND p.deleted_at IS NULL""", (rs, i) -> {
            BigDecimal s = rs.getBigDecimal("costo_pedido");
            BigDecimal h = rs.getBigDecimal("costo_almacen_pct");
            BigDecimal ns = rs.getBigDecimal("nivel_servicio");
            return new ParametrosProducto(rs.getLong("id"), rs.getString("nombre"), s, h, ns,
                    lng(rs, "proveedor_id"), rs.getString("nombre_empresa"), rs.getBoolean("activo"),
                    s != null ? s : bd(costoPedido), h != null ? h : bd(costoAlmacenPct),
                    ns != null ? ns : bd(nivelServicio));
        }, productoId).stream().findFirst();
    }

    @Override
    public void guardarParametros(Long productoId, GuardarParametrosComando c, String usuario) {
        jdbc.update("""
                INSERT INTO parametros_reorden (producto_id, costo_pedido, costo_almacen_pct, nivel_servicio,
                                                proveedor_id, activo, updated_by, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (producto_id) DO UPDATE SET
                    costo_pedido = EXCLUDED.costo_pedido, costo_almacen_pct = EXCLUDED.costo_almacen_pct,
                    nivel_servicio = EXCLUDED.nivel_servicio, proveedor_id = EXCLUDED.proveedor_id,
                    activo = EXCLUDED.activo, updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP""",
                productoId, c.costoPedido(), c.costoAlmacenPct(), c.nivelServicio(), c.proveedorId(), c.activo(),
                usuario);
    }

    @Override
    public boolean proveedorActivo(Long proveedorId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM proveedores WHERE id = ? AND activo AND deleted_at IS NULL)",
                Boolean.class, proveedorId));
    }

    // ================================================================== utilidades

    private static BigDecimal bd(double x) {
        return bd(x, 4);
    }

    private static BigDecimal bd(double x, int escala) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            return BigDecimal.ZERO.setScale(escala);
        }
        return BigDecimal.valueOf(x).setScale(escala, RoundingMode.HALF_UP);
    }

    private static BigDecimal num(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        if (v == null) {
            return null;
        }
        // 12.5000 -> 12.5 y 120.0000 -> 120 (sin notación científica 1.2E+2)
        BigDecimal limpio = v.stripTrailingZeros();
        return limpio.scale() < 0 ? limpio.setScale(0) : limpio;
    }

    private static Double dbl(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return v == null ? null : v.doubleValue();
    }

    private static Long lng(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? null : v;
    }

    private static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

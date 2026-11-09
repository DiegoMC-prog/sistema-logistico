package com.tienda.backend.credito.infrastructure.adapter.out.persistence;

import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.AplicacionAbono;
import com.tienda.backend.credito.application.model.ClienteCredito;
import com.tienda.backend.credito.application.model.CuentaRef;
import com.tienda.backend.credito.application.model.CuentaResumen;
import com.tienda.backend.credito.application.model.DeudorResumen;
import com.tienda.backend.credito.application.model.MetodoPagoAbono;
import com.tienda.backend.credito.application.port.out.CreditoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/** Persistencia del crédito con SQL directo: pocas tablas, consultas de agregación y bloqueos explícitos. */
@Component
@RequiredArgsConstructor
public class CreditoJdbcAdapter implements CreditoRepositoryPort {

    private static final String SQL_CLIENTE = """
            SELECT id, nombre, estado, credito_habilitado, limite_credito
            FROM clientes WHERE id = ? AND deleted_at IS NULL""";

    private static final RowMapper<ClienteCredito> CLIENTE = (rs, i) -> new ClienteCredito(rs.getLong("id"),
            rs.getString("nombre"), "ACTIVO".equals(rs.getString("estado")), rs.getBoolean("credito_habilitado"),
            rs.getBigDecimal("limite_credito"));

    private static final RowMapper<CuentaRef> CUENTA_REF = (rs, i) -> new CuentaRef(rs.getLong("id"),
            rs.getLong("cliente_id"), rs.getBigDecimal("monto_original"), rs.getBigDecimal("saldo"), rs.getString("estado"));

    private final JdbcTemplate jdbc;

    @Override
    public Optional<ClienteCredito> bloquearCliente(Long clienteId) {
        return jdbc.query(SQL_CLIENTE + " FOR UPDATE", CLIENTE, clienteId).stream().findFirst();
    }

    @Override
    public Optional<ClienteCredito> buscarCliente(Long clienteId) {
        return jdbc.query(SQL_CLIENTE, CLIENTE, clienteId).stream().findFirst();
    }

    @Override
    public BigDecimal deudaPendiente(Long clienteId) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(saldo), 0) FROM cuentas_por_cobrar WHERE cliente_id = ? AND estado = 'PENDIENTE'",
                BigDecimal.class, clienteId);
    }

    @Override
    public void insertarCuenta(Long clienteId, Long ventaId, BigDecimal monto, String usuario) {
        jdbc.update("""
                INSERT INTO cuentas_por_cobrar (cliente_id, venta_id, monto_original, saldo, estado, created_by, updated_by)
                VALUES (?, ?, ?, ?, 'PENDIENTE', ?, ?)""", clienteId, ventaId, monto, monto, usuario, usuario);
    }

    @Override
    public Optional<CuentaRef> bloquearCuentaDeVenta(Long ventaId) {
        return jdbc.query("""
                SELECT id, cliente_id, monto_original, saldo, estado
                FROM cuentas_por_cobrar WHERE venta_id = ? FOR UPDATE""", CUENTA_REF, ventaId).stream().findFirst();
    }

    @Override
    public boolean cuentaTieneAbonos(Long cuentaId) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM abonos_credito_aplicacion WHERE cuenta_id = ?", Integer.class, cuentaId);
        return n != null && n > 0;
    }

    @Override
    public void actualizarCuenta(Long cuentaId, BigDecimal saldo, String estado, String usuario) {
        jdbc.update("UPDATE cuentas_por_cobrar SET saldo = ?, estado = ?, updated_by = ?, updated_at = NOW() WHERE id = ?",
                saldo, estado, usuario, cuentaId);
    }

    @Override
    public List<CuentaRef> cuentasPendientes(Long clienteId) {
        return jdbc.query("""
                SELECT id, cliente_id, monto_original, saldo, estado FROM cuentas_por_cobrar
                WHERE cliente_id = ? AND estado = 'PENDIENTE' ORDER BY fecha, id FOR UPDATE""", CUENTA_REF, clienteId);
    }

    @Override
    public List<CuentaResumen> cuentasDeCliente(Long clienteId) {
        return jdbc.query("""
                SELECT c.id, c.venta_id, v.numero_venta, c.monto_original, c.saldo, c.estado, c.fecha
                FROM cuentas_por_cobrar c JOIN ventas v ON v.id = c.venta_id
                WHERE c.cliente_id = ? ORDER BY c.fecha DESC, c.id DESC""",
                (rs, i) -> new CuentaResumen(rs.getLong("id"), rs.getLong("venta_id"), rs.getString("numero_venta"),
                        rs.getBigDecimal("monto_original"), rs.getBigDecimal("saldo"), rs.getString("estado"),
                        rs.getTimestamp("fecha").toLocalDateTime()), clienteId);
    }

    @Override
    public List<DeudorResumen> deudores() {
        return jdbc.query("""
                SELECT cl.id, cl.nombre, cl.documento, cl.limite_credito, SUM(c.saldo) AS deuda, COUNT(*) AS cuentas
                FROM cuentas_por_cobrar c JOIN clientes cl ON cl.id = c.cliente_id
                WHERE c.estado = 'PENDIENTE'
                GROUP BY cl.id, cl.nombre, cl.documento, cl.limite_credito
                ORDER BY deuda DESC""",
                (rs, i) -> new DeudorResumen(rs.getLong("id"), rs.getString("nombre"), rs.getString("documento"),
                        rs.getBigDecimal("limite_credito"), rs.getBigDecimal("deuda"), rs.getInt("cuentas")));
    }

    @Override
    public Optional<MetodoPagoAbono> buscarMetodoPago(Long id) {
        return jdbc.query("SELECT id, nombre, estado FROM metodos_pago WHERE id = ? AND deleted_at IS NULL",
                (rs, i) -> new MetodoPagoAbono(rs.getLong("id"), rs.getString("nombre"), "ACTIVO".equals(rs.getString("estado"))),
                id).stream().findFirst();
    }

    @Override
    public Optional<Long> turnoAbiertoDeUsuario(Long usuarioId) {
        return jdbc.query("SELECT id FROM turnos_caja WHERE usuario_id = ? AND estado = 'ABIERTO' FOR SHARE",
                (rs, i) -> rs.getLong(1), usuarioId).stream().findFirst();
    }

    @Override
    public String siguienteNumeroAbono() {
        Long n = jdbc.queryForObject("SELECT nextval('seq_numero_abono')", Long.class);
        return String.format("AB-%08d", n);
    }

    @Override
    public Long insertarAbono(String numero, Long clienteId, Long turnoId, Long usuarioId, Long metodoPagoId,
                              BigDecimal monto, String referencia) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO abonos_credito (numero_abono, cliente_id, turno_id, usuario_id, metodo_pago_id, monto, referencia)
                    VALUES (?, ?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, numero);
            ps.setLong(2, clienteId);
            ps.setLong(3, turnoId);
            ps.setLong(4, usuarioId);
            ps.setLong(5, metodoPagoId);
            ps.setBigDecimal(6, monto);
            ps.setString(7, referencia);
            return ps;
        }, keys);
        Number id = (Number) keys.getKeys().get("id");
        return id.longValue();
    }

    @Override
    public void insertarAplicacion(Long abonoId, Long cuentaId, BigDecimal monto) {
        jdbc.update("INSERT INTO abonos_credito_aplicacion (abono_id, cuenta_id, monto) VALUES (?, ?, ?)",
                abonoId, cuentaId, monto);
    }

    private static final String SQL_ABONO = """
            SELECT a.id, a.numero_abono, a.cliente_id, cl.nombre AS cliente, a.turno_id, u.username AS usuario,
                   mp.nombre AS metodo, a.monto, a.referencia, a.fecha
            FROM abonos_credito a
            JOIN clientes cl ON cl.id = a.cliente_id
            JOIN usuarios u ON u.id = a.usuario_id
            JOIN metodos_pago mp ON mp.id = a.metodo_pago_id
            """;

    @Override
    public Optional<AbonoResumen> buscarAbono(Long abonoId) {
        return jdbc.query(SQL_ABONO + " WHERE a.id = ?", (rs, i) -> abono(rs), abonoId).stream().findFirst();
    }

    @Override
    public List<AbonoResumen> abonosDeCliente(Long clienteId) {
        return jdbc.query(SQL_ABONO + " WHERE a.cliente_id = ? ORDER BY a.fecha DESC, a.id DESC",
                (rs, i) -> abono(rs), clienteId);
    }

    private AbonoResumen abono(java.sql.ResultSet rs) throws java.sql.SQLException {
        long id = rs.getLong("id");
        List<AplicacionAbono> aplicaciones = jdbc.query("""
                SELECT ap.cuenta_id, v.numero_venta, ap.monto
                FROM abonos_credito_aplicacion ap
                JOIN cuentas_por_cobrar c ON c.id = ap.cuenta_id
                JOIN ventas v ON v.id = c.venta_id
                WHERE ap.abono_id = ? ORDER BY ap.id""",
                (r, i) -> new AplicacionAbono(r.getLong(1), r.getString(2), r.getBigDecimal(3)), id);
        return new AbonoResumen(id, rs.getString("numero_abono"), rs.getLong("cliente_id"), rs.getString("cliente"),
                rs.getLong("turno_id"), rs.getString("usuario"), rs.getString("metodo"), rs.getBigDecimal("monto"),
                rs.getString("referencia"), rs.getTimestamp("fecha").toLocalDateTime(), aplicaciones);
    }
}

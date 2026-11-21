package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.lote.application.model.AjusteCostoLote;
import com.tienda.backend.lote.application.port.out.AjusteCostoLotePort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AjusteCostoLoteJdbcAdapter implements AjusteCostoLotePort {

    private final JdbcTemplate jdbc;

    @Override
    public void registrar(Long loteId, BigDecimal costoAnterior, BigDecimal costoNuevo, String motivo, String usuario) {
        jdbc.update("""
                INSERT INTO ajustes_costo_lote (lote_id, costo_anterior, costo_nuevo, motivo, usuario)
                VALUES (?, ?, ?, ?, ?)""", loteId, costoAnterior, costoNuevo, motivo, usuario);
    }

    @Override
    public List<AjusteCostoLote> deLote(Long loteId) {
        return jdbc.query("""
                SELECT id, lote_id, costo_anterior, costo_nuevo, motivo, usuario, fecha
                FROM ajustes_costo_lote WHERE lote_id = ? ORDER BY fecha DESC, id DESC""",
                (rs, i) -> new AjusteCostoLote(rs.getLong("id"), rs.getLong("lote_id"),
                        limpio(rs.getBigDecimal("costo_anterior")), limpio(rs.getBigDecimal("costo_nuevo")),
                        rs.getString("motivo"), rs.getString("usuario"), rs.getTimestamp("fecha").toLocalDateTime()),
                loteId);
    }

    private static BigDecimal limpio(BigDecimal v) {
        BigDecimal l = v.stripTrailingZeros();
        return l.scale() < 0 ? l.setScale(0) : l;
    }
}

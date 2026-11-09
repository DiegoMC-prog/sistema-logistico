package com.tienda.backend.tipocliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.tipocliente.application.port.out.UsoTipoClienteConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Comprueba si el tipo de cliente está en uso consultando directamente las tablas de clientes, precios y ventas.
 * Los módulos de precios y ventas todavía no existen; cuando se creen, este adaptador pasa a usar sus repositorios
 * y el servicio no cambia.
 */
@Component
@RequiredArgsConstructor
public class UsoTipoClienteConsultaAdapter implements UsoTipoClienteConsultaPort {
    private static final String SQL = """
            SELECT EXISTS (SELECT 1 FROM clientes WHERE tipo_cliente_id = ? AND deleted_at IS NULL)
                OR EXISTS (SELECT 1 FROM precios_presentacion WHERE tipo_cliente_id = ? AND deleted_at IS NULL)
                OR EXISTS (SELECT 1 FROM ventas WHERE tipo_cliente_id = ?)
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean estaEnUso(Long tipoClienteId) {
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(SQL, Boolean.class, tipoClienteId, tipoClienteId, tipoClienteId));
    }
}

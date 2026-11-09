package com.tienda.backend.proveedor.infrastructure.adapter.out.persistence;

import com.tienda.backend.proveedor.application.port.out.PedidoConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Comprueba si el proveedor tiene pedidos consultando la tabla directamente (sin acoplarse al módulo de pedidos). */
@Component
@RequiredArgsConstructor
public class ProveedorPedidoConsultaAdapter implements PedidoConsultaPort {
    private static final String SQL =
            "SELECT EXISTS (SELECT 1 FROM pedidos_proveedor WHERE proveedor_id = ? AND deleted_at IS NULL)";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean existenPedidosDelProveedor(Long proveedorId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(SQL, Boolean.class, proveedorId));
    }
}

package com.tienda.backend.cliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.cliente.application.port.out.VentaConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Consulta directamente la tabla de ventas hasta que exista el módulo de ventas; entonces pasa a usar su repositorio.
 * Las ventas no tienen borrado lógico, así que cuenta cualquier venta histórica.
 */
@Component
@RequiredArgsConstructor
public class ClienteVentaConsultaAdapter implements VentaConsultaPort {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean clienteTieneVentas(Long clienteId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM ventas WHERE cliente_id = ?)", Boolean.class, clienteId));
    }
}

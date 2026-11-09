package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.port.out.VentaDevolucionConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Consulta las devoluciones directamente en la tabla, para que ventas no dependa del módulo de devoluciones. */
@Component
@RequiredArgsConstructor
public class VentaDevolucionConsultaAdapter implements VentaDevolucionConsultaPort {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean tieneDevoluciones(Long ventaId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM devoluciones_venta WHERE venta_id = ?)", Boolean.class, ventaId));
    }

    @Override
    public boolean esVentaDeCambio(Long ventaId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM devoluciones_venta WHERE venta_cambio_id = ?)", Boolean.class, ventaId));
    }
}

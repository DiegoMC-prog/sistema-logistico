package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import com.tienda.backend.auth.application.port.out.BitacoraAutorizacionPort;
import com.tienda.backend.shared.application.model.AutorizacionOtorgada;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BitacoraAutorizacionJdbcAdapter implements BitacoraAutorizacionPort {

    private final JdbcTemplate jdbc;

    @Override
    public void registrar(AutorizacionOtorgada a, Long referenciaId) {
        jdbc.update("""
                INSERT INTO autorizaciones_operacion (operacion, referencia_id, monto, umbral, solicitado_por, autorizado_por)
                VALUES (?, ?, ?, ?, ?, ?)""", a.operacion().name(), referenciaId, a.monto(), a.umbral(),
                a.solicitadoPor(), a.autorizadoPor());
    }
}

package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import com.tienda.backend.auth.application.port.out.BloqueoCuentaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/** Columnas intentos_fallidos y bloqueado_hasta de usuarios (migración V11). */
@Component
@RequiredArgsConstructor
public class BloqueoCuentaJdbcAdapter implements BloqueoCuentaPort {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<LocalDateTime> bloqueadaHasta(Long usuarioId) {
        return jdbc.query("SELECT bloqueado_hasta FROM usuarios WHERE id = ? AND bloqueado_hasta IS NOT NULL",
                (rs, i) -> rs.getTimestamp(1).toLocalDateTime(), usuarioId).stream().findFirst();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean registrarFallo(Long usuarioId, int maximo, LocalDateTime hasta) {
        // Un solo UPDATE atómico: dos intentos simultáneos no se pisan el contador
        Boolean bloqueada = jdbc.query("""
                UPDATE usuarios SET
                    intentos_fallidos = CASE WHEN intentos_fallidos + 1 >= ? THEN 0 ELSE intentos_fallidos + 1 END,
                    bloqueado_hasta = CASE WHEN intentos_fallidos + 1 >= ? THEN ? ELSE bloqueado_hasta END
                WHERE id = ?
                RETURNING intentos_fallidos = 0""", rs -> rs.next() && rs.getBoolean(1),
                maximo, maximo, Timestamp.valueOf(hasta), usuarioId);
        return Boolean.TRUE.equals(bloqueada);
    }

    @Override
    public void reiniciar(Long usuarioId) {
        jdbc.update("""
                UPDATE usuarios SET intentos_fallidos = 0, bloqueado_hasta = NULL
                WHERE id = ? AND (intentos_fallidos <> 0 OR bloqueado_hasta IS NOT NULL)""", usuarioId);
    }
}

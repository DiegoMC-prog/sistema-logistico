package com.tienda.backend.auth.infrastructure.adapter.out.revocacion;

import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.infrastructure.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Lista de revocados en Redis: compartida entre instancias y, con persistencia, sobrevive a reinicios.
 * Cada entrada ({@code jwt:revocado:{usuarioId}}) caduca sola con el TTL, sin código de limpieza.
 *
 * <p>Si Redis no responde, la comprobación <b>acepta</b> el token y registra el error: la API sigue disponible y, como
 * los refresh tokens ya están revocados en BD, el usuario revocado no puede renovar su sesión (el riesgo se limita a
 * la vida restante de su access token).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seguridad.revocacion", havingValue = "redis")
public class RevocacionRedisAdapter implements RevocacionTokensPort {
    private static final String PREFIJO = "jwt:revocado:";

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RevocacionRedisAdapter(StringRedisTemplate redis, JwtProperties propiedades) {
        this.redis = redis;
        this.ttl = Duration.ofMinutes(propiedades.accessMinutos()).plusMinutes(1);
    }

    @Override
    public void revocar(Long usuarioId) {
        try {
            redis.opsForValue().set(PREFIJO + usuarioId, String.valueOf(System.currentTimeMillis()), ttl);
        } catch (DataAccessException e) {
            log.error("No se pudo registrar la revocación del usuario {} en Redis: {}", usuarioId, e.getMessage());
        }
    }

    @Override
    public boolean estaRevocado(Long usuarioId, long emitidoEnMs) {
        try {
            String marca = redis.opsForValue().get(PREFIJO + usuarioId);
            return marca != null && emitidoEnMs < Long.parseLong(marca);
        } catch (DataAccessException e) {
            log.warn("Redis no responde; se acepta el token del usuario {}: {}", usuarioId, e.getMessage());
            return false;
        }
    }
}

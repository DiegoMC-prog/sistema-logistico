package com.tienda.backend.auth.infrastructure.adapter.out.revocacion;

import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.infrastructure.config.JwtProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lista de revocados en memoria: sirve con una sola instancia del backend. Se vacía al reiniciar
 * (los tokens revocados vuelven a valer hasta que expiren, como máximo la vida del access token).
 */
@Component
@ConditionalOnProperty(name = "app.seguridad.revocacion", havingValue = "memoria", matchIfMissing = true)
public class RevocacionEnMemoriaAdapter implements RevocacionTokensPort {
    private final ConcurrentHashMap<Long, Long> marcasMs = new ConcurrentHashMap<>();
    private final long vidaMarcaMs;

    public RevocacionEnMemoriaAdapter(JwtProperties propiedades) {
        // Pasada la vida de un token, todos los emitidos antes de la marca ya expiraron: la marca deja de hacer falta.
        this.vidaMarcaMs = Duration.ofMinutes(propiedades.accessMinutos()).plusMinutes(1).toMillis();
    }

    @Override
    public void revocar(Long usuarioId) {
        long ahora = System.currentTimeMillis();
        marcasMs.put(usuarioId, ahora);
        marcasMs.values().removeIf(marca -> ahora - marca > vidaMarcaMs);
    }

    @Override
    public boolean estaRevocado(Long usuarioId, long emitidoEnMs) {
        Long marca = marcasMs.get(usuarioId);
        return marca != null && emitidoEnMs < marca;
    }
}

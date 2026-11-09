package com.tienda.backend.auth.infrastructure.adapter.out.limite;

import com.tienda.backend.auth.application.port.out.LimitadorSolicitudesPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ventana deslizante en memoria: como máximo {@value #MAXIMO} solicitudes por clave en {@code VENTANA}. Sirve para una
 * sola instancia de la aplicación (igual que la revocación de tokens en modo "memoria"); con varias instancias habría
 * que llevar el conteo en Redis. Detrás de un proxy, {@code getRemoteAddr()} es la IP del proxy y el límite sería global.
 */
@Component
public class LimitadorSolicitudesMemoriaAdapter implements LimitadorSolicitudesPort {
    static final int MAXIMO = 5;
    static final Duration VENTANA = Duration.ofMinutes(15);
    /** Si el mapa crece más que esto, se descartan las claves ya vencidas para que no ocupe memoria sin límite. */
    private static final int LIMPIEZA_A_PARTIR_DE = 10_000;

    private final Map<String, Deque<Instant>> solicitudes = new ConcurrentHashMap<>();

    @Override
    public boolean permitir(String clave) {
        return permitir(clave, Instant.now());
    }

    boolean permitir(String clave, Instant ahora) {
        if (solicitudes.size() > LIMPIEZA_A_PARTIR_DE) {
            limpiar(ahora);
        }
        Deque<Instant> cola = solicitudes.computeIfAbsent(clave, k -> new ArrayDeque<>());
        synchronized (cola) {
            Instant limite = ahora.minus(VENTANA);
            while (!cola.isEmpty() && cola.peekFirst().isBefore(limite)) {
                cola.pollFirst();
            }
            if (cola.size() >= MAXIMO) {
                return false;
            }
            cola.addLast(ahora);
            return true;
        }
    }

    private void limpiar(Instant ahora) {
        Instant limite = ahora.minus(VENTANA);
        solicitudes.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return e.getValue().isEmpty() || e.getValue().peekLast().isBefore(limite);
            }
        });
    }
}

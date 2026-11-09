package com.tienda.backend.auth.application.port.out;

public interface LimitadorSolicitudesPort {
    /** Registra una solicitud de {@code clave} (p. ej. una IP) y dice si todavía está dentro del límite. */
    boolean permitir(String clave);
}

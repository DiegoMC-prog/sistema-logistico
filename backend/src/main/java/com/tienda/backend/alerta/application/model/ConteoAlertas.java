package com.tienda.backend.alerta.application.model;

/** Alertas activas (no resueltas) sin leer, en total y por tipo: alimenta el contador de la campana. */
public record ConteoAlertas(long total, long vencimiento, long vencido, long stockBajo) {
}

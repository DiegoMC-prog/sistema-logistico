package com.tienda.backend.alerta.application.model;

/** Resumen de una pasada del generador de alertas. */
public record ResultadoGeneracion(int vencimiento, int vencido, int stockBajo, int resueltas) {
    public int creadas() {
        return vencimiento + vencido + stockBajo;
    }
}

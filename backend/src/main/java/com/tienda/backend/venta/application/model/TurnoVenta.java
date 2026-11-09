package com.tienda.backend.venta.application.model;

public record TurnoVenta(Long turnoId, Long cajaId, Long depositoId, boolean abierto) {
}

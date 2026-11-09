package com.tienda.backend.venta.application.port.out;

import com.tienda.backend.venta.application.model.TurnoVenta;

import java.util.Optional;

public interface TurnoVentaPort {
    Optional<TurnoVenta> turnoAbiertoDeUsuario(Long usuarioId);

    /** Bloquea el turno en modo compartido hasta el fin de la transacción y devuelve su estado ya vigente. */
    Optional<TurnoVenta> bloquearCompartido(Long turnoId);
}

package com.tienda.backend.devolucion.application.port.out;

import com.tienda.backend.devolucion.application.model.TurnoRef;

import java.math.BigDecimal;
import java.util.Optional;

public interface TurnoDevolucionPort {
    Optional<TurnoRef> turnoAbiertoDeUsuario(Long usuarioId);

    /** Bloquea el turno para escribir (serializa reembolsos en efectivo, egresos y cierre) y devuelve su estado vigente. */
    Optional<TurnoRef> bloquearParaEscritura(Long turnoId);

    BigDecimal efectivoDisponible(Long turnoId);

    /** Saca efectivo de la caja del turno (el saldo a favor que se entrega en un cambio). Falla si no alcanza. */
    void registrarEgreso(Long turnoId, BigDecimal monto, String motivo);
}

package com.tienda.backend.auth.application.port.out;

import java.time.LocalDateTime;
import java.util.Optional;

/** R06: intentos fallidos de inicio de sesión y bloqueo temporal de la cuenta. */
public interface BloqueoCuentaPort {

    /** Hasta cuándo está bloqueada la cuenta, si lo está. */
    Optional<LocalDateTime> bloqueadaHasta(Long usuarioId);

    /**
     * Suma un intento fallido en una transacción propia (sobrevive aunque la operación que lo causó se revierta). Al
     * llegar a {@code maximo} bloquea la cuenta hasta {@code hasta} y reinicia el contador.
     *
     * @return true si con este intento la cuenta quedó bloqueada
     */
    boolean registrarFallo(Long usuarioId, int maximo, LocalDateTime hasta);

    /** Borra los intentos fallidos y el bloqueo (inicio de sesión correcto o contraseña restablecida). */
    void reiniciar(Long usuarioId);
}

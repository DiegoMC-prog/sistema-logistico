package com.tienda.backend.auth.domain.exception;

import com.tienda.backend.shared.domain.exception.DemasiadasSolicitudesException;

/** R06: la cuenta quedó bloqueada por intentos fallidos (se responde 429). */
public class CuentaBloqueadaException extends DemasiadasSolicitudesException {
    public CuentaBloqueadaException(long minutos) {
        super("La cuenta está bloqueada por demasiados intentos fallidos. Intente de nuevo en "
              + Math.max(minutos, 1) + (minutos == 1 ? " minuto" : " minutos")
              + " o restablezca su contraseña.");
    }
}

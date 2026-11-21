package com.tienda.backend.shared.domain.exception;

/**
 * R08: las credenciales del administrador no sirven (incorrectas, sin permiso de autorizar o cuenta bloqueada).
 * Se responde 403 con {@code code = AUTORIZACION_INVALIDA}; nunca 401, que el frontend tomaría como sesión vencida.
 */
public class AutorizacionInvalidaException extends DomainException {

    public static final String CODIGO = "AUTORIZACION_INVALIDA";

    public AutorizacionInvalidaException(String message) {
        super(message);
    }
}

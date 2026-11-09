package com.tienda.backend.auth.domain.exception;

import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;

/** El enlace de restablecimiento no existe, ya se usó, venció o su cuenta ya no está activa. No dice cuál de los casos. */
public class TokenRestablecimientoInvalidoException extends InvalidBusinessRuleException {
    public TokenRestablecimientoInvalidoException() {
        super("El enlace para restablecer la contraseña no es válido o ya venció. Solicite uno nuevo.");
    }
}

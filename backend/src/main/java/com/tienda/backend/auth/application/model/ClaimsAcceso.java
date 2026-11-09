package com.tienda.backend.auth.application.model;

import java.util.List;

/** Contenido ya validado de un access token. {@code emitidoEnMs} es la hora de emisión con precisión de milisegundos. */
public record ClaimsAcceso(
        Long usuarioId,
        String username,
        List<String> roles,
        List<String> permisos,
        boolean passwordTemporal,
        long emitidoEnMs
) {
}

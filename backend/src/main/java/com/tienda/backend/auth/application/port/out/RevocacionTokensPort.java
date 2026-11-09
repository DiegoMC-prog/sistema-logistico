package com.tienda.backend.auth.application.port.out;

/**
 * Lista de usuarios cuyos access tokens emitidos antes de cierta hora ya no valen. Permite revocar un JWT
 * al instante sin consultar la BD en cada petición. Las entradas caducan solas al cumplirse la vida del token.
 */
public interface RevocacionTokensPort {
    /** Anota "ahora" como marca de revocación del usuario. */
    void revocar(Long usuarioId);

    /** true si el token se emitió antes de la marca de revocación del usuario. */
    boolean estaRevocado(Long usuarioId, long emitidoEnMs);
}

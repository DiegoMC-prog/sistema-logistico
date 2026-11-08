package com.tienda.backend.auth.application.port.in;

/** Uso interno: lo invocan los listeners de eventos cuando cambia el acceso de un usuario o de un rol. */
public interface RevocarAccesoUseCase {
    void revocarAccesoDeUsuario(Long usuarioId);

    void revocarAccesoDeRol(Long rolId);
}

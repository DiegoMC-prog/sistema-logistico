package com.tienda.backend.shared.application.model;

/** Identidad mínima del usuario autenticado. La implementa el principal de seguridad; la auditoría la usa para saber quién actúa. */
public interface IdentidadUsuario {
    Long id();
}

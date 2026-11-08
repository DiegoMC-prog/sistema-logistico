package com.tienda.backend.shared.application.event;

/**
 * Cambió el nombre o los permisos de un rol, lo que afecta a los tokens ya emitidos de todos sus usuarios.
 */
public record AccesoDeRolModificado(Long rolId) {
}

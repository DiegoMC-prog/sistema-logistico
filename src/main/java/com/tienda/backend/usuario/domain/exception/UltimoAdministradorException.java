package com.tienda.backend.usuario.domain.exception;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;

public class UltimoAdministradorException extends BusinessConflictException {
    public UltimoAdministradorException() {
        super("No se puede desactivar, eliminar ni quitar el rol ADMIN al último administrador activo.");
    }
}

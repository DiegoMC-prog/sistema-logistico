package com.tienda.backend.usuario.application.port.in;

import com.tienda.backend.usuario.application.model.UsuarioDetalle;

import java.util.Set;

public interface AsignarRolesUsuarioUseCase {
    /** Reemplaza el conjunto completo de roles del usuario. */
    UsuarioDetalle asignarRoles(Long usuarioId, Set<Long> rolIds);
}

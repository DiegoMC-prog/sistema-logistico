package com.tienda.backend.rol.application.port.in;

import com.tienda.backend.rol.application.model.RolDetalle;

import java.util.Set;

public interface AsignarPermisosRolUseCase {
    /** Reemplaza el conjunto completo de permisos del rol. */
    RolDetalle asignarPermisos(Long rolId, Set<Long> permisoIds);
}

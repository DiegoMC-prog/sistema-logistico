package com.tienda.backend.rol.application.port.out;

import com.tienda.backend.permiso.domain.model.Permiso;

import java.util.Collection;
import java.util.List;

public interface PermisoConsultaPort {
    /** Devuelve los permisos que existen entre los ids pedidos (los inexistentes simplemente no aparecen). */
    List<Permiso> obtenerPorIds(Collection<Long> permisoIds);
}

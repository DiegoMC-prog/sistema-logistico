package com.tienda.backend.usuario.application.port.out;

import com.tienda.backend.rol.domain.model.Rol;

import java.util.Collection;
import java.util.List;

public interface RolConsultaPort {
    /** Devuelve los roles que existen entre los ids pedidos (los inexistentes simplemente no aparecen). */
    List<Rol> obtenerPorIds(Collection<Long> rolIds);
}

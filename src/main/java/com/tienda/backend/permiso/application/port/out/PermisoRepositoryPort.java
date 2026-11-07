package com.tienda.backend.permiso.application.port.out;

import com.tienda.backend.permiso.domain.model.Permiso;

import java.util.Collection;
import java.util.List;

public interface PermisoRepositoryPort {
    List<Permiso> findAll();
    List<Permiso> findAllById(Collection<Long> ids);
}

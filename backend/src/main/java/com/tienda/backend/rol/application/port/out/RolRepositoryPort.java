package com.tienda.backend.rol.application.port.out;

import com.tienda.backend.rol.application.model.RolFiltro;
import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RolRepositoryPort {
    Rol save(Rol rol);
    Optional<Rol> findById(Long id);
    List<Rol> findAllById(Collection<Long> ids);
    PageResult<Rol> findAll(RolFiltro filtro, int page, int size);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
    Set<Long> findPermisoIdsByRolId(Long rolId);
    void reemplazarPermisos(Long rolId, Set<Long> permisoIds);
}

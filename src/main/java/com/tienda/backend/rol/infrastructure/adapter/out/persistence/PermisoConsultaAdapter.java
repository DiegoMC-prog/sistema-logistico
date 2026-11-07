package com.tienda.backend.rol.infrastructure.adapter.out.persistence;

import com.tienda.backend.permiso.application.port.out.PermisoRepositoryPort;
import com.tienda.backend.permiso.domain.model.Permiso;
import com.tienda.backend.rol.application.port.out.PermisoConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PermisoConsultaAdapter implements PermisoConsultaPort {
    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    public List<Permiso> obtenerPorIds(Collection<Long> permisoIds) {
        if (permisoIds == null || permisoIds.isEmpty()) {
            return List.of();
        }
        return permisoRepositoryPort.findAllById(permisoIds);
    }
}

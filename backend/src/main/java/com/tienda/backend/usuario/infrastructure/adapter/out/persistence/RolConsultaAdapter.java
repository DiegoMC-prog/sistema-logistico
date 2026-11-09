package com.tienda.backend.usuario.infrastructure.adapter.out.persistence;

import com.tienda.backend.rol.application.port.out.RolRepositoryPort;
import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.usuario.application.port.out.RolConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RolConsultaAdapter implements RolConsultaPort {
    private final RolRepositoryPort rolRepositoryPort;

    @Override
    public List<Rol> obtenerPorIds(Collection<Long> rolIds) {
        if (rolIds == null || rolIds.isEmpty()) {
            return List.of();
        }
        return rolRepositoryPort.findAllById(rolIds);
    }
}

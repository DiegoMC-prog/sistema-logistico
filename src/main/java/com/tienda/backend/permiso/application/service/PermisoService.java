package com.tienda.backend.permiso.application.service;

import com.tienda.backend.permiso.application.port.in.ListarPermisoUseCase;
import com.tienda.backend.permiso.application.port.out.PermisoRepositoryPort;
import com.tienda.backend.permiso.domain.model.Permiso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermisoService implements ListarPermisoUseCase {

    private final PermisoRepositoryPort permisoRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> listar() {
        return permisoRepositoryPort.findAll();
    }
}

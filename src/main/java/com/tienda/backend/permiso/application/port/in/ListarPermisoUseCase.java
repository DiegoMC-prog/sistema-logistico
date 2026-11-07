package com.tienda.backend.permiso.application.port.in;

import com.tienda.backend.permiso.domain.model.Permiso;

import java.util.List;

public interface ListarPermisoUseCase {
    List<Permiso> listar();
}

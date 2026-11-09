package com.tienda.backend.rol.application.port.in;

import com.tienda.backend.rol.domain.model.Rol;

public interface EditarRolUseCase {
    Rol editar(Long id, Rol rol);
}

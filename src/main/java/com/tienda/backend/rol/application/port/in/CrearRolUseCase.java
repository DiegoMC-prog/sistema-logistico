package com.tienda.backend.rol.application.port.in;

import com.tienda.backend.rol.domain.model.Rol;

public interface CrearRolUseCase {
    Rol create(Rol rol);
}

package com.tienda.backend.rol.application.port.in;

import com.tienda.backend.rol.application.model.RolDetalle;

public interface ObtenerRolUseCase {
    RolDetalle obtener(Long id);
}

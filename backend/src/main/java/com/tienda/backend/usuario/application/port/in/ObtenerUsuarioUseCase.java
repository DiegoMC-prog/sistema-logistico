package com.tienda.backend.usuario.application.port.in;

import com.tienda.backend.usuario.application.model.UsuarioDetalle;

public interface ObtenerUsuarioUseCase {
    UsuarioDetalle obtener(Long id);
}

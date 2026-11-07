package com.tienda.backend.usuario.application.port.in;

import com.tienda.backend.usuario.application.model.UsuarioDetalle;

public interface CambiarEstadoUsuarioUseCase {
    UsuarioDetalle activar(Long id);
    UsuarioDetalle desactivar(Long id);
}

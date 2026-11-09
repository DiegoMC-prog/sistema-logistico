package com.tienda.backend.usuario.application.port.in;

import com.tienda.backend.usuario.application.model.CrearUsuarioComando;
import com.tienda.backend.usuario.application.model.UsuarioDetalle;

public interface CrearUsuarioUseCase {
    UsuarioDetalle create(CrearUsuarioComando comando);
}

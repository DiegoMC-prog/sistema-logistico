package com.tienda.backend.usuario.application.port.in;

import com.tienda.backend.usuario.application.model.EditarUsuarioComando;
import com.tienda.backend.usuario.application.model.UsuarioDetalle;

public interface EditarUsuarioUseCase {
    UsuarioDetalle editar(Long id, EditarUsuarioComando comando);
}

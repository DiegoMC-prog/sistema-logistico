package com.tienda.backend.usuario.application.port.in;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.usuario.application.model.UsuarioDetalle;
import com.tienda.backend.usuario.application.model.UsuarioFiltro;

public interface ListarUsuarioUseCase {
    PageResult<UsuarioDetalle> listar(UsuarioFiltro filtro, int page, int size);
}

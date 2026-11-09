package com.tienda.backend.rol.application.port.in;

import com.tienda.backend.rol.application.model.RolFiltro;
import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarRolUseCase {
    PageResult<Rol> listar(RolFiltro filtro, int page, int size);
}

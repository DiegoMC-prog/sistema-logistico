package com.tienda.backend.proveedor.application.port.in;

import com.tienda.backend.proveedor.application.model.ProveedorFiltro;
import com.tienda.backend.proveedor.domain.model.Proveedor;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarProveedorUseCase {
    PageResult<Proveedor> listar(ProveedorFiltro filtro, int page, int size);
}

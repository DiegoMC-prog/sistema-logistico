package com.tienda.backend.tipocliente.application.port.in;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.tipocliente.application.model.TipoClienteFiltro;
import com.tienda.backend.tipocliente.domain.model.TipoCliente;

public interface ListarTipoClienteUseCase {
    PageResult<TipoCliente> listar(TipoClienteFiltro filtro, int page, int size);
}

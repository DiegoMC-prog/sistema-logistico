package com.tienda.backend.cliente.application.port.in;

import com.tienda.backend.cliente.application.model.ClienteDetalle;
import com.tienda.backend.cliente.application.model.ClienteFiltro;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarClienteUseCase {
    PageResult<ClienteDetalle> listar(ClienteFiltro filtro, int page, int size);
}

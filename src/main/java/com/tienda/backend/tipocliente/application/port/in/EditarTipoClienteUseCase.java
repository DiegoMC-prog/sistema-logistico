package com.tienda.backend.tipocliente.application.port.in;

import com.tienda.backend.tipocliente.domain.model.TipoCliente;

public interface EditarTipoClienteUseCase {
    TipoCliente editar(Long id, TipoCliente tipoCliente);
}

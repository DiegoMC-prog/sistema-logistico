package com.tienda.backend.tipocliente.application.port.in;

import com.tienda.backend.tipocliente.domain.model.TipoCliente;

public interface CrearTipoClienteUseCase {
    TipoCliente create(TipoCliente tipoCliente);
}

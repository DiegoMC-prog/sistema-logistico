package com.tienda.backend.tipocliente.application.port.in;

import com.tienda.backend.tipocliente.domain.model.TipoCliente;

public interface CambiarEstadoTipoClienteUseCase {
    TipoCliente activar(Long id);
    TipoCliente desactivar(Long id);
}

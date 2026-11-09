package com.tienda.backend.cliente.application.port.in;

import com.tienda.backend.cliente.application.model.ClienteDetalle;

public interface CambiarEstadoClienteUseCase {
    ClienteDetalle activar(Long id);
    ClienteDetalle desactivar(Long id);
}

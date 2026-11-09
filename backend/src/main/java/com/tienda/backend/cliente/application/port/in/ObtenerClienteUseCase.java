package com.tienda.backend.cliente.application.port.in;

import com.tienda.backend.cliente.application.model.ClienteDetalle;

public interface ObtenerClienteUseCase {
    ClienteDetalle obtener(Long id);
}

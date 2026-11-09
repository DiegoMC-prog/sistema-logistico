package com.tienda.backend.cliente.application.port.in;

import com.tienda.backend.cliente.application.model.ClienteDetalle;
import com.tienda.backend.cliente.domain.model.Cliente;

public interface CrearClienteUseCase {
    ClienteDetalle create(Cliente cliente);
}

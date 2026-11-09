package com.tienda.backend.cliente.application.port.out;

import com.tienda.backend.cliente.application.model.TipoClienteRef;

import java.util.Optional;

public interface TipoClienteConsultaPort {
    Optional<TipoClienteRef> buscarPorId(Long id);

    /** Tipo de sistema que se asigna cuando el cliente no indica uno. */
    TipoClienteRef obtenerGeneral();
}

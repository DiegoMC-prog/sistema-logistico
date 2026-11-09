package com.tienda.backend.cliente.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.cliente.application.model.ClienteDetalle;
import com.tienda.backend.cliente.domain.model.Cliente;

public record ClienteResponse(
        Long id,
        TipoClienteResumen tipoCliente,
        String documento,
        String nombre,
        String telefono,
        String email,
        String estado
) {
    public record TipoClienteResumen(Long id, String nombre) {
    }

    public static ClienteResponse from(ClienteDetalle d) {
        Cliente c = d.cliente();
        return new ClienteResponse(c.getId(), new TipoClienteResumen(c.getTipoClienteId(), d.tipoClienteNombre()),
                c.getDocumento(), c.getNombre(), c.getTelefono(), c.getEmail(), c.getEstado().name());
    }
}

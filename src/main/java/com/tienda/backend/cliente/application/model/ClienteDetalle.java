package com.tienda.backend.cliente.application.model;

import com.tienda.backend.cliente.domain.model.Cliente;

/** Cliente junto con el nombre de su tipo, para no obligar al frontend a una segunda consulta. */
public record ClienteDetalle(Cliente cliente, String tipoClienteNombre) {
}

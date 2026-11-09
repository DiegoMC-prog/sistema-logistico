package com.tienda.backend.venta.application.port.out;

import com.tienda.backend.venta.application.model.ClienteVenta;

import java.util.Optional;

public interface ClienteVentaPort {
    Optional<ClienteVenta> buscarPorId(Long id);
}

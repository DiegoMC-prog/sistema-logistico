package com.tienda.backend.venta.application.port.out;

import com.tienda.backend.venta.application.model.MetodoPagoRef;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface MetodoPagoVentaPort {
    Map<Long, MetodoPagoRef> buscarPorIds(Collection<Long> ids);

    Optional<MetodoPagoRef> buscarPorNombre(String nombre);
}

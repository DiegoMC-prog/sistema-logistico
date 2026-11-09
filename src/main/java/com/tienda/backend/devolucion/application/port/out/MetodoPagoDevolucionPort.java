package com.tienda.backend.devolucion.application.port.out;

import com.tienda.backend.devolucion.application.model.MetodoPagoRef;

import java.util.Optional;

public interface MetodoPagoDevolucionPort {
    Optional<MetodoPagoRef> buscarPorId(Long id);
    Optional<MetodoPagoRef> buscarPorNombre(String nombre);
}

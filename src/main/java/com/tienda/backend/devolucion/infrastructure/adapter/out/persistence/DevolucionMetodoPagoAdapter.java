package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.application.model.MetodoPagoRef;
import com.tienda.backend.devolucion.application.port.out.MetodoPagoDevolucionPort;
import com.tienda.backend.metodopago.application.port.out.MetodoPagoRepositoryPort;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DevolucionMetodoPagoAdapter implements MetodoPagoDevolucionPort {
    private final MetodoPagoRepositoryPort metodoPagoRepositoryPort;

    @Override
    public Optional<MetodoPagoRef> buscarPorId(Long id) {
        return metodoPagoRepositoryPort.findById(id).map(this::aRef);
    }

    @Override
    public Optional<MetodoPagoRef> buscarPorNombre(String nombre) {
        return metodoPagoRepositoryPort.findByNombre(nombre).map(this::aRef);
    }

    private MetodoPagoRef aRef(MetodoPago m) {
        return new MetodoPagoRef(m.getId(), m.getNombre(), m.estaActivo());
    }
}

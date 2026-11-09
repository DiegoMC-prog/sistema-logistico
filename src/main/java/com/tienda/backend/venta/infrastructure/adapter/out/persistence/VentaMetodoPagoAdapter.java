package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.model.MetodoPagoRef;
import com.tienda.backend.venta.application.port.out.MetodoPagoVentaPort;
import com.tienda.backend.metodopago.application.port.out.MetodoPagoRepositoryPort;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VentaMetodoPagoAdapter implements MetodoPagoVentaPort {
    private final MetodoPagoRepositoryPort metodoPagoRepositoryPort;

    @Override
    public Map<Long, MetodoPagoRef> buscarPorIds(Collection<Long> ids) {
        return metodoPagoRepositoryPort.findAllById(ids).stream()
                .collect(Collectors.toMap(MetodoPago::getId, m -> new MetodoPagoRef(m.getId(), m.getNombre(), m.estaActivo())));
    }

    @Override
    public java.util.Optional<MetodoPagoRef> buscarPorNombre(String nombre) {
        return metodoPagoRepositoryPort.findByNombre(nombre)
                .map(m -> new MetodoPagoRef(m.getId(), m.getNombre(), m.estaActivo()));
    }
}

package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.model.ClienteVenta;
import com.tienda.backend.venta.application.port.out.ClienteVentaPort;
import com.tienda.backend.cliente.application.port.out.ClienteRepositoryPort;
import com.tienda.backend.cliente.domain.model.EstadoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VentaClienteAdapter implements ClienteVentaPort {
    private final ClienteRepositoryPort clienteRepositoryPort;

    @Override
    public Optional<ClienteVenta> buscarPorId(Long id) {
        return clienteRepositoryPort.findById(id).map(c -> new ClienteVenta(c.getId(), c.getNombre(),
                c.getTipoClienteId(), c.getEstado() == EstadoCliente.ACTIVO));
    }
}

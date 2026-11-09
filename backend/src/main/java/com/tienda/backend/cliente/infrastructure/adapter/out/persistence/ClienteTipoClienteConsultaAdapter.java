package com.tienda.backend.cliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.cliente.application.model.TipoClienteRef;
import com.tienda.backend.cliente.application.port.out.TipoClienteConsultaPort;
import com.tienda.backend.tipocliente.application.port.out.TipoClienteRepositoryPort;
import com.tienda.backend.tipocliente.domain.exception.TipoClienteNotFoundException;
import com.tienda.backend.tipocliente.domain.model.TipoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClienteTipoClienteConsultaAdapter implements TipoClienteConsultaPort {
    private final TipoClienteRepositoryPort tipoClienteRepositoryPort;

    @Override
    public Optional<TipoClienteRef> buscarPorId(Long id) {
        return tipoClienteRepositoryPort.findById(id).map(this::ref);
    }

    @Override
    public TipoClienteRef obtenerGeneral() {
        return tipoClienteRepositoryPort.findByNombre(TipoCliente.NOMBRE_GENERAL)
                .map(this::ref)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe el tipo de cliente de sistema " + TipoCliente.NOMBRE_GENERAL));
    }

    private TipoClienteRef ref(TipoCliente t) {
        return new TipoClienteRef(t.getId(), t.getNombre(), t.estaActivo());
    }
}

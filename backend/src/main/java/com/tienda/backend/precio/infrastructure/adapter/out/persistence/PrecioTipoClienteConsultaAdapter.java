package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import com.tienda.backend.precio.application.model.TipoClienteRef;
import com.tienda.backend.precio.application.port.out.PrecioTipoClienteConsultaPort;
import com.tienda.backend.tipocliente.application.port.out.TipoClienteRepositoryPort;
import com.tienda.backend.tipocliente.domain.model.TipoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PrecioTipoClienteConsultaAdapter implements PrecioTipoClienteConsultaPort {
    private final TipoClienteRepositoryPort tipoClienteRepositoryPort;

    @Override
    public Optional<TipoClienteRef> buscarPorId(Long id) {
        return tipoClienteRepositoryPort.findById(id)
                .map(t -> new TipoClienteRef(t.getId(), t.getNombre(), t.estaActivo()));
    }

    @Override
    public Map<Long, String> nombresPorIds(Collection<Long> ids) {
        return tipoClienteRepositoryPort.findAllById(ids).stream()
                .collect(Collectors.toMap(TipoCliente::getId, TipoCliente::getNombre));
    }
}

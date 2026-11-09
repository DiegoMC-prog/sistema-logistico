package com.tienda.backend.inventario.infrastructure.adapter.out.consulta;

import com.tienda.backend.inventario.application.port.out.LoteConsultaPort;
import com.tienda.backend.lote.application.port.out.LoteRepositoryPort;
import com.tienda.backend.lote.domain.model.Lote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventarioLoteConsultaAdapter implements LoteConsultaPort {
    private final LoteRepositoryPort loteRepositoryPort;

    @Override
    public Optional<Lote> obtenerPorId(Long loteId) {
        return loteRepositoryPort.findById(loteId);
    }

    @Override
    public List<Lote> obtenerPorIds(Collection<Long> loteIds) {
        return loteIds.isEmpty() ? List.of() : loteRepositoryPort.findAllById(loteIds);
    }
}

package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.lote.domain.model.Lote;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LoteConsultaPort {
    Optional<Lote> obtenerPorId(Long loteId);

    List<Lote> obtenerPorIds(Collection<Long> loteIds);
}

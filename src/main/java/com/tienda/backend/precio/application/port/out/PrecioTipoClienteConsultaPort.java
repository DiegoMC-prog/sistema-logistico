package com.tienda.backend.precio.application.port.out;

import com.tienda.backend.precio.application.model.TipoClienteRef;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface PrecioTipoClienteConsultaPort {
    Optional<TipoClienteRef> buscarPorId(Long id);

    /** Nombre de cada tipo de cliente indicado, en una sola consulta. */
    Map<Long, String> nombresPorIds(Collection<Long> ids);
}

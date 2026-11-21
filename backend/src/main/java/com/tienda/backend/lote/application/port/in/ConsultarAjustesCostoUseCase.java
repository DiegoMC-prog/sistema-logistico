package com.tienda.backend.lote.application.port.in;

import com.tienda.backend.lote.application.model.AjusteCostoLote;

import java.util.List;

/** R10: historial de correcciones del costo de un lote. */
public interface ConsultarAjustesCostoUseCase {
    List<AjusteCostoLote> ajustesCosto(Long loteId);
}

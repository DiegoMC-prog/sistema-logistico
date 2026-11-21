package com.tienda.backend.lote.application.port.out;

import com.tienda.backend.lote.application.model.AjusteCostoLote;

import java.math.BigDecimal;
import java.util.List;

/** Tabla ajustes_costo_lote (migración V11): solo se inserta. */
public interface AjusteCostoLotePort {

    void registrar(Long loteId, BigDecimal costoAnterior, BigDecimal costoNuevo, String motivo, String usuario);

    /** Ajustes del lote, el más reciente primero. */
    List<AjusteCostoLote> deLote(Long loteId);
}

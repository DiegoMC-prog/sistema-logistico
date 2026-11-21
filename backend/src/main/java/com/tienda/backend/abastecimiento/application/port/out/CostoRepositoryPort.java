package com.tienda.backend.abastecimiento.application.port.out;

import com.tienda.backend.abastecimiento.application.model.CostoFiltro;
import com.tienda.backend.abastecimiento.application.model.HistoricoCosto;

import java.time.LocalDate;
import java.util.List;

/** Lectura del histórico de costos (vista v_historico_costos_proveedor). */
public interface CostoRepositoryPort {

    /**
     * Compras confirmadas desde {@code desde} (inclusive), ordenadas por proveedor, producto y fecha ascendente.
     * La variación contra la compra anterior viene en null: la calcula el servicio.
     */
    List<HistoricoCosto> historial(CostoFiltro filtro, LocalDate desde);
}

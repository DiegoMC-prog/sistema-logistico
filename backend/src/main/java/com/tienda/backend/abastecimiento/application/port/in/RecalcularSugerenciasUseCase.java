package com.tienda.backend.abastecimiento.application.port.in;

import com.tienda.backend.abastecimiento.application.model.CalculoResumen;
import com.tienda.backend.abastecimiento.application.model.OrigenCalculo;

/** Evalúa todos los productos activos y guarda un nuevo juego de sugerencias. */
public interface RecalcularSugerenciasUseCase {
    CalculoResumen recalcular(OrigenCalculo origen);

    boolean existeCalculo();
}

package com.tienda.backend.abastecimiento.application.port.in;

import com.tienda.backend.abastecimiento.application.model.CalculoResumen;
import com.tienda.backend.abastecimiento.application.model.SugerenciaFiltro;
import com.tienda.backend.abastecimiento.application.model.SugerenciaResumen;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface ConsultarSugerenciasUseCase {
    Optional<CalculoResumen> ultimoCalculo();

    /** Sugerencias del último cálculo. */
    PageResult<SugerenciaResumen> listar(SugerenciaFiltro filtro, int page, int size);

    SugerenciaResumen obtener(Long id);
}

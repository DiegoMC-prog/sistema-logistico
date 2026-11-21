package com.tienda.backend.abastecimiento.application.model;

import com.tienda.backend.abastecimiento.domain.model.AccionSugerencia;
import com.tienda.backend.abastecimiento.domain.model.EstadoSugerencia;

/** Filtros sobre las sugerencias del último cálculo; null = sin filtro. */
public record SugerenciaFiltro(EstadoSugerencia estado, AccionSugerencia accion, Long proveedorId, String search) {
}

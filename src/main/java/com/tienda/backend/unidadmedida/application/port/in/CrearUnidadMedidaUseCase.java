package com.tienda.backend.unidadmedida.application.port.in;

import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;

public interface CrearUnidadMedidaUseCase {
    UnidadMedida create(UnidadMedida unidadMedida);
}

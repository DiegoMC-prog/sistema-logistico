package com.tienda.backend.unidadmedida.application.port.in;

import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;

public interface EditarUnidadMedidaUseCase {
    UnidadMedida editar(Long id, UnidadMedida unidadMedida);
}

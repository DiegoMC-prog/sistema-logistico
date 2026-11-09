package com.tienda.backend.presentacion.application.port.out;

import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;

import java.util.Optional;

public interface UnidadMedidaConsultaPort {
    Optional<UnidadMedida> obtenerPorId(Long unidadMedidaId);
}

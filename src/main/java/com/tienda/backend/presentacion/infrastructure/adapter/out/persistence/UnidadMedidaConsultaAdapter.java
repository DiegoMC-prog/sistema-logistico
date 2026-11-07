package com.tienda.backend.presentacion.infrastructure.adapter.out.persistence;

import com.tienda.backend.presentacion.application.port.out.UnidadMedidaConsultaPort;
import com.tienda.backend.unidadmedida.application.port.out.UnidadMedidaRepositoryPort;
import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UnidadMedidaConsultaAdapter implements UnidadMedidaConsultaPort {
    private final UnidadMedidaRepositoryPort unidadMedidaRepositoryPort;

    @Override
    public Optional<UnidadMedida> obtenerPorId(Long unidadMedidaId) {
        return unidadMedidaRepositoryPort.findById(unidadMedidaId);
    }
}

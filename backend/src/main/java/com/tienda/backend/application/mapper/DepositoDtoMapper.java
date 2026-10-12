package com.tienda.backend.application.mapper;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;
import com.tienda.backend.domain.model.Deposito;
import org.springframework.stereotype.Component;

@Component
public class DepositoDtoMapper {

    public Deposito toDomain(DepositoRequest request) {
        return Deposito.crear(
                request.nombre(),
                request.descripcion()
        );
    }

    public DepositoResponse toResponse(Deposito domain) {
        return new DepositoResponse(
                domain.getId(),
                domain.getNombre(),
                domain.getDescripcion(),
                domain.getEstado()
        );
    }
}

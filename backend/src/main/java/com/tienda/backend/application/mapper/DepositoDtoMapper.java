package com.tienda.backend.application.mapper;

import com.tienda.backend.application.dto.request.DepositoRequest;
import com.tienda.backend.application.dto.response.DepositoResponse;
import com.tienda.backend.domain.model.Deposito;
import org.springframework.stereotype.Component;

@Component
public class DepositoDtoMapper {
    public Deposito toDomain(DepositoRequest request) {
        return new Deposito(request.nombre(), request.descripcion());
    }

    public DepositoResponse toResponse(Deposito deposito) {
        return new DepositoResponse(
                deposito.getId(),
                deposito.getNombre(),
                deposito.getDescripcion(),
                deposito.getCreatedAt(),
                deposito.getUpdatedAt()
        );
    }
}

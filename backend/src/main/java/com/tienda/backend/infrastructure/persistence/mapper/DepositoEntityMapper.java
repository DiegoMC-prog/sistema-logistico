package com.tienda.backend.infrastructure.persistence.mapper;

import com.tienda.backend.domain.model.Deposito;
import com.tienda.backend.infrastructure.persistence.entity.DepositoEntity;
import org.springframework.stereotype.Component;

@Component
public class DepositoEntityMapper {

    public DepositoEntity toEntity(Deposito domain) {
        if (domain == null) {
            return null;
        }

        return new DepositoEntity(
                domain.getId(),
                domain.getNombre(),
                domain.getDescripcion(),
                domain.getEstado()
        );
    }

    public Deposito toDomain(DepositoEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Deposito(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getEstado()
        );
    }
}

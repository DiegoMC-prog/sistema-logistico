package com.tienda.backend.infrastructure.persistence.mapper;

import com.tienda.backend.domain.model.Deposito;
import com.tienda.backend.infrastructure.persistence.entity.DepositoEntity;
import org.springframework.stereotype.Component;

@Component
public class DepositoEntityMapper {
    public DepositoEntity toEntity(Deposito deposito) {
        return new DepositoEntity(
                deposito.getId(),
                deposito.getNombre(),
                deposito.getDescripcion(),
                deposito.getActivo(),
                deposito.getCreatedAt(),
                deposito.getUpdatedAt()
        );
    }

    public Deposito toDomain(DepositoEntity entity) {
        return new Deposito(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getActivo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

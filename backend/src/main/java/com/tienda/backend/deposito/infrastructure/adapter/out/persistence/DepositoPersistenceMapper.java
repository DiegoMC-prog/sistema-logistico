package com.tienda.backend.deposito.infrastructure.adapter.out.persistence;

import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.deposito.domain.model.EstadoDeposito;
import org.springframework.stereotype.Component;

@Component
public class DepositoPersistenceMapper {
    public Deposito toDomain(DepositoEntity entity) {
        if (entity == null) return null;

        EstadoDeposito estado = entity.getEstado() != null
                ? EstadoDeposito.valueOf(entity.getEstado())
                : EstadoDeposito.ACTIVO;

        return new Deposito(entity.getId(), entity.getNombre(), entity.getDescripcion(), estado);
    }

    public DepositoEntity toEntity(Deposito domain) {
        if (domain == null) return null;

        return DepositoEntity.builder()
                .id(domain.getId())
                .nombre(domain.getNombre())
                .descripcion(domain.getDescripcion())
                .estado(domain.getEstado().name())
                .build();
    }
}

package com.tienda.backend.inventario.infrastructure.adapter.out.consulta;

import com.tienda.backend.deposito.application.port.out.DepositoRepositoryPort;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.inventario.application.port.out.DepositoConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventarioDepositoConsultaAdapter implements DepositoConsultaPort {
    private final DepositoRepositoryPort depositoRepositoryPort;

    @Override
    public Optional<Deposito> obtenerPorId(Long depositoId) {
        return depositoRepositoryPort.findById(depositoId);
    }

    @Override
    public List<Deposito> obtenerPorIds(Collection<Long> depositoIds) {
        return depositoIds.isEmpty() ? List.of() : depositoRepositoryPort.findAllById(depositoIds);
    }
}

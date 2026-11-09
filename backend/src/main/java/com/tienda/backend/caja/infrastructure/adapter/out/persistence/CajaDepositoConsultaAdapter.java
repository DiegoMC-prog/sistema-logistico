package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.application.model.DepositoRef;
import com.tienda.backend.caja.application.port.out.CajaDepositoConsultaPort;
import com.tienda.backend.deposito.application.port.out.DepositoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CajaDepositoConsultaAdapter implements CajaDepositoConsultaPort {
    private final DepositoRepositoryPort depositoRepositoryPort;

    @Override
    public Optional<DepositoRef> buscarPorId(Long id) {
        return depositoRepositoryPort.findById(id)
                .map(d -> new DepositoRef(d.getId(), d.getNombre(), d.estaActivo()));
    }
}

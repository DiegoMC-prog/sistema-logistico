package com.tienda.backend.deposito.infrastructure.adapter.out.persistence;

import com.tienda.backend.deposito.application.port.out.InventarioConsultaPort;
import com.tienda.backend.inventario.application.port.out.InventarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositoInventarioConsultaAdapter implements InventarioConsultaPort {
    private final InventarioRepositoryPort inventarioRepositoryPort;

    @Override
    public boolean tieneStock(Long depositoId) {
        return inventarioRepositoryPort.existeStockPorDeposito(depositoId);
    }
}

package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.inventario.application.port.out.InventarioRepositoryPort;
import com.tienda.backend.lote.application.port.out.InventarioConsultaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoteInventarioConsultaAdapter implements InventarioConsultaPort {
    private final InventarioRepositoryPort inventarioRepositoryPort;

    @Override
    public boolean tieneStock(Long loteId) {
        return inventarioRepositoryPort.existeStockPorLote(loteId);
    }
}

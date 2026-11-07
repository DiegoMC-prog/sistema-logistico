package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.lote.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.presentacion.application.port.out.PresentacionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LotePresentacionConsultaAdapter implements PresentacionConsultaPort {
    private final PresentacionRepositoryPort presentacionRepositoryPort;

    @Override
    public boolean existeBasePorProducto(Long productoId) {
        return presentacionRepositoryPort.existsBaseByProductoId(productoId);
    }
}

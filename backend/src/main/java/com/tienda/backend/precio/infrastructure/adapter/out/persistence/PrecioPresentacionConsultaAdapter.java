package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import com.tienda.backend.precio.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.presentacion.application.port.out.PresentacionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrecioPresentacionConsultaAdapter implements PresentacionConsultaPort {
    private final PresentacionRepositoryPort presentacionRepositoryPort;

    @Override
    public boolean existe(Long presentacionId) {
        return presentacionRepositoryPort.findById(presentacionId).isPresent();
    }
}

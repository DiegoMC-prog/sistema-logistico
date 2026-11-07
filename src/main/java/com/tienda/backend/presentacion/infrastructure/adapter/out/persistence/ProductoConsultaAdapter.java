package com.tienda.backend.presentacion.infrastructure.adapter.out.persistence;

import com.tienda.backend.presentacion.application.port.out.ProductoConsultaPort;
import com.tienda.backend.producto.application.port.out.ProductoRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoConsultaAdapter implements ProductoConsultaPort {
    private final ProductoRepositoryPort productoRepositoryPort;

    @Override
    public boolean existePorId(Long productoId) {
        return productoRepositoryPort.findById(productoId).isPresent();
    }
}

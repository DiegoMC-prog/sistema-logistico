package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.lote.application.port.out.ProductoConsultaPort;
import com.tienda.backend.producto.application.port.out.ProductoRepositoryPort;
import com.tienda.backend.producto.domain.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoteProductoConsultaAdapter implements ProductoConsultaPort {
    private final ProductoRepositoryPort productoRepositoryPort;

    @Override
    public Optional<Producto> obtenerPorId(Long productoId) {
        return productoRepositoryPort.findById(productoId);
    }
}

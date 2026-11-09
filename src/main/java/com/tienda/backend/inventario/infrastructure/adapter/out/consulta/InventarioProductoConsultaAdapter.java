package com.tienda.backend.inventario.infrastructure.adapter.out.consulta;

import com.tienda.backend.inventario.application.port.out.ProductoConsultaPort;
import com.tienda.backend.producto.application.port.out.ProductoRepositoryPort;
import com.tienda.backend.producto.domain.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventarioProductoConsultaAdapter implements ProductoConsultaPort {
    private final ProductoRepositoryPort productoRepositoryPort;

    @Override
    public Optional<Producto> obtenerPorId(Long productoId) {
        return productoRepositoryPort.findById(productoId);
    }

    @Override
    public List<Producto> obtenerPorIds(Collection<Long> productoIds) {
        return productoIds.isEmpty() ? List.of() : productoRepositoryPort.findAllById(productoIds);
    }
}

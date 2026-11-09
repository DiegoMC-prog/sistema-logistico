package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.model.PresentacionVenta;
import com.tienda.backend.venta.application.port.out.CatalogoVentaPort;
import com.tienda.backend.presentacion.application.port.out.PresentacionRepositoryPort;
import com.tienda.backend.producto.application.port.out.ProductoRepositoryPort;
import com.tienda.backend.producto.domain.model.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VentaCatalogoAdapter implements CatalogoVentaPort {
    private final PresentacionRepositoryPort presentacionRepositoryPort;
    private final ProductoRepositoryPort productoRepositoryPort;

    @Override
    public Optional<PresentacionVenta> buscarPresentacion(Long presentacionId) {
        return presentacionRepositoryPort.findById(presentacionId).flatMap(pres ->
                productoRepositoryPort.findById(pres.getProductoId()).map(prod -> new PresentacionVenta(
                        pres.getId(), prod.getId(), prod.getNombre(), prod.estaActivo(), pres.getFactorConversion())));
    }

    @Override
    public Map<Long, String> nombresProducto(Collection<Long> productoIds) {
        return productoRepositoryPort.findAllById(productoIds).stream()
                .collect(Collectors.toMap(Producto::getId, Producto::getNombre));
    }
}

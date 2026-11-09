package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.producto.domain.model.Producto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductoConsultaPort {
    Optional<Producto> obtenerPorId(Long productoId);

    List<Producto> obtenerPorIds(Collection<Long> productoIds);
}

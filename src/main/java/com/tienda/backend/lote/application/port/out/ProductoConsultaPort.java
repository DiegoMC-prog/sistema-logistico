package com.tienda.backend.lote.application.port.out;

import com.tienda.backend.producto.domain.model.Producto;

import java.util.Optional;

public interface ProductoConsultaPort {
    Optional<Producto> obtenerPorId(Long productoId);
}

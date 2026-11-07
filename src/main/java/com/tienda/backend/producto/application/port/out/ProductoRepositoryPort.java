package com.tienda.backend.producto.application.port.out;

import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.application.model.ProductoFiltro;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface ProductoRepositoryPort {
    Producto save(Producto producto);
    PageResult<ProductoDetalle> findAll(ProductoFiltro filtro, int page, int size);
    Optional<Producto> findById(Long id);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}

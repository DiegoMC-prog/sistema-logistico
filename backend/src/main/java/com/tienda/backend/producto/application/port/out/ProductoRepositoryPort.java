package com.tienda.backend.producto.application.port.out;

import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.application.model.ProductoFiltro;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

public interface ProductoRepositoryPort {
    Producto save(Producto producto);
    PageResult<ProductoDetalle> findAll(ProductoFiltro filtro, int page, int size);
    Optional<Producto> findById(Long id);
    List<Producto> findAllById(Collection<Long> ids);
    void deleteById(Long id);
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}

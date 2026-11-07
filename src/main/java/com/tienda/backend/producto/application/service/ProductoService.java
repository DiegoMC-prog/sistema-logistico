package com.tienda.backend.producto.application.service;

import com.tienda.backend.categoria.domain.exception.CategoriaNotFoundException;
import com.tienda.backend.producto.application.port.in.CambiarEstadoProductoUseCase;
import com.tienda.backend.producto.application.port.in.CrearProductoUseCase;
import com.tienda.backend.producto.application.port.in.EditarProductoUseCase;
import com.tienda.backend.producto.application.port.in.EliminarProductoUseCase;
import com.tienda.backend.producto.application.port.in.ListarProductoUseCase;
import com.tienda.backend.producto.application.port.in.ObtenerProductoUseCase;
import com.tienda.backend.producto.application.port.out.CategoriaConsultaPort;
import com.tienda.backend.producto.application.port.out.ProductoRepositoryPort;
import com.tienda.backend.producto.domain.exception.ProductoNombreDuplicadoException;
import com.tienda.backend.producto.domain.exception.ProductoNotFoundException;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.producto.application.model.ProductoDetalle;
import com.tienda.backend.producto.application.model.ProductoFiltro;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProductoService implements CrearProductoUseCase, EditarProductoUseCase, ListarProductoUseCase,
        ObtenerProductoUseCase, EliminarProductoUseCase, CambiarEstadoProductoUseCase {

    private final ProductoRepositoryPort productoRepositoryPort;
    private final CategoriaConsultaPort categoriaConsultaPort;

    @Override
    @Transactional
    public ProductoDetalle create(Producto producto) {
        String categoriaNombre = nombreCategoria(producto.getCategoriaId());
        if (productoRepositoryPort.existsByNombre(producto.getNombre())) {
            throw new ProductoNombreDuplicadoException(producto.getNombre());
        }
        return new ProductoDetalle(productoRepositoryPort.save(producto), categoriaNombre);
    }

    @Override
    @Transactional
    public ProductoDetalle editar(Long id, Producto datos) {
        Producto existente = buscar(id);

        String categoriaNombre = nombreCategoria(datos.getCategoriaId());
        if (productoRepositoryPort.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new ProductoNombreDuplicadoException(datos.getNombre());
        }

        existente.editar(datos.getNombre(), datos.getDescripcion(), datos.getCategoriaId(),
                datos.getStockMinimo(), datos.isEsPerecedero());

        return new ProductoDetalle(productoRepositoryPort.save(existente), categoriaNombre);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ProductoDetalle> listar(ProductoFiltro filtro, int page, int size) {
        return productoRepositoryPort.findAll(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDetalle obtener(Long id) {
        return detalle(buscar(id));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        buscar(id);
        productoRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public ProductoDetalle activar(Long id) {
        Producto producto = buscar(id);
        producto.activar();
        return detalle(productoRepositoryPort.save(producto));
    }

    @Override
    @Transactional
    public ProductoDetalle desactivar(Long id) {
        Producto producto = buscar(id);
        producto.desactivar();
        return detalle(productoRepositoryPort.save(producto));
    }

    private Producto buscar(Long id) {
        return productoRepositoryPort.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    private String nombreCategoria(Long categoriaId) {
        return categoriaConsultaPort.obtenerNombrePorId(categoriaId)
                .orElseThrow(() -> new CategoriaNotFoundException(categoriaId));
    }

    private ProductoDetalle detalle(Producto producto) {
        return new ProductoDetalle(producto, nombreCategoria(producto.getCategoriaId()));
    }
}

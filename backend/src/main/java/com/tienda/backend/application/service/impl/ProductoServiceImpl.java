package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.ProductoRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.ProductoResponse;
import com.tienda.backend.application.dto.response.ProductoStatsResponse;
import com.tienda.backend.application.mapper.ProductoDtoMapper;
import com.tienda.backend.application.service.ProductoService;
import com.tienda.backend.domain.exception.RecursoNoEncontrado;
import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.domain.model.Producto;
import com.tienda.backend.domain.repository.CategoriaRepository;
import com.tienda.backend.domain.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoDtoMapper mapper;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               CategoriaRepository categoriaRepository,
                               ProductoDtoMapper mapper) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = obtenerCategoriaOError(request.categoriaId());

        if (productoRepository.existePorCodigoBarras(request.codigoBarras())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el código de barras '" + request.codigoBarras() + "'.");
        }

        Producto producto = mapper.toDomain(request, categoria);
        Producto guardado = productoRepository.guardar(producto);

        return mapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return productoRepository.buscarPorId(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el producto con ID " + id + "."));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listar(int page, int size, String busqueda, Long categoriaId, Boolean estado) {
        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' debe ser mayor o igual a 0.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El parámetro 'size' debe estar entre 1 y 100.");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("nombre").ascending().and(Sort.by("id").ascending()));

        boolean sinFiltros = (busqueda == null || busqueda.isBlank())
                && categoriaId == null
                && estado == null;

        Page<Producto> resultado = sinFiltros
                ? productoRepository.listarTodos(pageable)
                : productoRepository.buscarPorFiltros(
                        busqueda != null && !busqueda.isBlank() ? busqueda.trim() : null,
                        categoriaId,
                        estado,
                        pageable);

        return PageResponse.from(resultado.map(mapper::toResponse));
    }

    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el producto con ID " + id + "."));

        Categoria categoria = obtenerCategoriaOError(request.categoriaId());

        if (productoRepository.existePorCodigoBarrasYIdDistinto(request.codigoBarras(), id)) {
            throw new IllegalArgumentException(
                    "Ya existe otro producto con el código de barras '" + request.codigoBarras() + "'.");
        }

        producto.actualizar(
                request.codigoBarras(),
                request.nombre(),
                request.descripcion(),
                request.imagen(),
                categoria);

        Producto actualizado = productoRepository.guardar(producto);
        return mapper.toResponse(actualizado);
    }

    @Override
    public ProductoResponse cambiarEstado(Long id, boolean estado) {
        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el producto con ID " + id + "."));

        producto.cambiarEstado(estado);
        Producto actualizado = productoRepository.guardar(producto);
        return mapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (productoRepository.buscarPorId(id).isEmpty()) {
            throw new RecursoNoEncontrado("No se puede eliminar: no existe un producto con ID " + id + ".");
        }

        productoRepository.eliminar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoStatsResponse obtenerEstadisticas() {
        long totalProductos = productoRepository.contarTotal();
        long activos = productoRepository.contarActivos();
        long inactivos = productoRepository.contarInactivos();

        return new ProductoStatsResponse(totalProductos, activos, inactivos);
    }

    private Categoria obtenerCategoriaOError(Long categoriaId) {
        return categoriaRepository.buscarPorId(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró la categoría con ID " + categoriaId + "."));
    }
}

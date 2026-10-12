package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.ProductoRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.ProductoResponse;
import com.tienda.backend.application.dto.response.ProductoStatsResponse;
import com.tienda.backend.application.mapper.ProductoDtoMapper;
import com.tienda.backend.application.service.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoDtoMapper mapper;
    private final FileStorageService fileStorageService;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               CategoriaRepository categoriaRepository,
                               ProductoDtoMapper mapper,
                               FileStorageService fileStorageService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public ProductoResponse crear(ProductoRequest request, MultipartFile imagen) {
        Categoria categoria = obtenerCategoriaOError(request.categoriaId());

        if (productoRepository.existePorCodigoBarras(request.codigoBarras())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto con el código de barras '" + request.codigoBarras() + "'.");
        }

        String rutaImagen = request.imagen();
        if (imagen != null && !imagen.isEmpty()) {
            rutaImagen = fileStorageService.almacenar(imagen, "productos");
        }

        Producto producto = new Producto(
                null,
                request.codigoBarras(),
                request.nombre(),
                request.descripcion(),
                rutaImagen,
                categoria,
                true
        );
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
    public ProductoResponse actualizar(Long id, ProductoRequest request, MultipartFile imagen) {
        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el producto con ID " + id + "."));

        Categoria categoria = obtenerCategoriaOError(request.categoriaId());

        if (productoRepository.existePorCodigoBarrasYIdDistinto(request.codigoBarras(), id)) {
            throw new IllegalArgumentException(
                    "Ya existe otro producto con el código de barras '" + request.codigoBarras() + "'.");
        }

        String rutaImagen = producto.getImagen();
        if (imagen != null && !imagen.isEmpty()) {
            if (rutaImagen != null && !rutaImagen.isBlank()) {
                fileStorageService.eliminar(rutaImagen);
            }
            rutaImagen = fileStorageService.almacenar(imagen, "productos");
        } else if (request.imagen() != null) {
            rutaImagen = request.imagen();
        }

        producto.actualizar(
                request.codigoBarras(),
                request.nombre(),
                request.descripcion(),
                rutaImagen,
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
        Producto producto = productoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se puede eliminar: no existe un producto con ID " + id + "."));

        if (producto.getImagen() != null && !producto.getImagen().isBlank()) {
            fileStorageService.eliminar(producto.getImagen());
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

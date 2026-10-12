package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.CategoriaRequest;
import com.tienda.backend.application.dto.response.CategoriaResponse;
import com.tienda.backend.application.dto.response.CategoriaStatsResponse;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.mapper.CategoriaDtoMapper;
import com.tienda.backend.application.service.CategoriaService;
import com.tienda.backend.domain.exception.RecursoNoEncontrado;
import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.domain.repository.CategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final CategoriaDtoMapper mapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaDtoMapper mapper) {
        this.categoriaRepository = categoriaRepository;
        this.mapper = mapper;
    }

    @Override
    public CategoriaResponse crear(CategoriaRequest categoriaRequest) {
        if (categoriaRepository.existePorNombre(categoriaRequest.nombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre.");
        }

        Categoria categoria = mapper.toDomain(categoriaRequest);
        Categoria guardada = categoriaRepository.guardar(categoria);

        return mapper.toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        return categoriaRepository.buscarPorId(id)
                .map(categoria -> {
                    long total = categoriaRepository.contarProductosPorCategoriaId(id);
                    categoria.asignarTotalProductos(total);
                    return mapper.toResponse(categoria);
                })
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró la categoría con ID " + id + "."));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoriaResponse> listar(int page, int size, String nombre) {
        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' debe ser mayor o igual a 0.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El parámetro 'size' debe estar entre 1 y 100.");
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("nombre").ascending().and(Sort.by("id").ascending()));
        Page<Categoria> resultado = (nombre == null || nombre.isBlank())
                ? categoriaRepository.listarTodos(pageable)
                : categoriaRepository.buscarPorNombre(nombre.trim(), pageable);

        List<Long> ids = resultado.getContent().stream()
                .map(Categoria::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Long> conteos = ids.isEmpty() ? Map.of() : categoriaRepository.contarProductosPorCategoriaIds(ids);

        Page<CategoriaResponse> paginaMapeada = resultado.map(categoria -> {
            long total = conteos.getOrDefault(categoria.getId(), 0L);
            categoria.asignarTotalProductos(total);
            return mapper.toResponse(categoria);
        });

        return PageResponse.from(paginaMapeada);
    }

    @Override
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró la categoría con ID " + id + "."));

        categoria.actualizar(request.nombre());
        Categoria actualizada = categoriaRepository.guardar(categoria);

        long total = categoriaRepository.contarProductosPorCategoriaId(id);
        actualizada.asignarTotalProductos(total);

        return mapper.toResponse(actualizada);
    }

    @Override
    public void eliminar(Long id) {
        if (categoriaRepository.buscarPorId(id).isEmpty()) {
            throw new RecursoNoEncontrado("No se puede eliminar: no existe una categoría con ID " + id + ".");
        }

        categoriaRepository.eliminar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaStatsResponse obtenerEstadisticas() {
        long totalCategorias = categoriaRepository.contarTotal();
        long categoriasEnUso = categoriaRepository.contarCategoriasEnUso();

        return new CategoriaStatsResponse(totalCategorias, categoriasEnUso);
    }
}

package com.tienda.backend.infrastructure.persistence.adapter;

import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.domain.repository.CategoriaRepository;
import com.tienda.backend.infrastructure.persistence.entity.CategoriaEntity;
import com.tienda.backend.infrastructure.persistence.mapper.CategoriaEntityMapper;
import com.tienda.backend.infrastructure.persistence.repository.CategoriaJpaRepository;
import com.tienda.backend.infrastructure.persistence.repository.ProductoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CategoriaRepositoryAdapter implements CategoriaRepository {

    private final CategoriaJpaRepository categoriaJpaRepository;
    private final ProductoJpaRepository productoJpaRepository;
    private final CategoriaEntityMapper mapper;

    public CategoriaRepositoryAdapter(CategoriaJpaRepository categoriaJpaRepository,
                                      ProductoJpaRepository productoJpaRepository,
                                      CategoriaEntityMapper mapper) {
        this.categoriaJpaRepository = categoriaJpaRepository;
        this.productoJpaRepository = productoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        CategoriaEntity entity = mapper.toEntity(categoria);
        CategoriaEntity guardado = categoriaJpaRepository.save(entity);

        return mapper.toDomain(guardado);
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Categoria> listarTodos(Pageable pageable) {
        return categoriaJpaRepository.findAll(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Categoria> buscarPorNombre(String nombre, Pageable pageable) {
        return categoriaJpaRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return categoriaJpaRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public void eliminar(Long id) {
        categoriaJpaRepository.deleteById(id);
    }

    @Override
    public long contarTotal() {
        return categoriaJpaRepository.count();
    }

    @Override
    public long contarCategoriasEnUso() {
        return productoJpaRepository.countCategoriasEnUso();
    }

    @Override
    public long contarProductosPorCategoriaId(Long categoriaId) {
        return productoJpaRepository.countByCategoriaId(categoriaId);
    }

    @Override
    public Map<Long, Long> contarProductosPorCategoriaIds(List<Long> categoriaIds) {
        if (categoriaIds == null || categoriaIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> resultados = productoJpaRepository.countProductosByCategoriaIds(categoriaIds);
        Map<Long, Long> conteos = new HashMap<>();
        for (Object[] fila : resultados) {
            Long catId = (Long) fila[0];
            Long total = ((Number) fila[1]).longValue();
            conteos.put(catId, total);
        }
        return conteos;
    }
}

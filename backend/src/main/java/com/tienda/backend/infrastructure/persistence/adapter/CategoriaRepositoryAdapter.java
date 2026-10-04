package com.tienda.backend.infrastructure.persistence.adapter;

import com.tienda.backend.domain.model.Categoria;
import com.tienda.backend.domain.repository.CategoriaRepository;
import com.tienda.backend.infrastructure.persistence.entity.CategoriaEntity;
import com.tienda.backend.infrastructure.persistence.mapper.CategoriaEntityMapper;
import com.tienda.backend.infrastructure.persistence.repository.CategoriaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CategoriaRepositoryAdapter implements CategoriaRepository {
    private final CategoriaJpaRepository categoriaJpaRepository;
    private final CategoriaEntityMapper entityMapper;

    public CategoriaRepositoryAdapter(CategoriaJpaRepository categoriaJpaRepository, CategoriaEntityMapper categoriaEntityMapper) {
        this.categoriaJpaRepository = categoriaJpaRepository;
        this.entityMapper = categoriaEntityMapper;
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        CategoriaEntity entity = entityMapper.toEntity(categoria);
        CategoriaEntity guardada = categoriaJpaRepository.save(entity);

        return entityMapper.toDomain(guardada);
    }

    @Override
    public Optional<Categoria> buscarPorId(Long id) {
        return categoriaJpaRepository.findById(id)
                .map(entityMapper::toDomain);
    }

    @Override
    public List<Categoria> buscarTodas() {
        return categoriaJpaRepository.findAll()
                .stream()
                .map(entityMapper::toDomain)
                .toList();
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
    public boolean existePorId(Long id) {
        return categoriaJpaRepository.existsById(id);
    }
}

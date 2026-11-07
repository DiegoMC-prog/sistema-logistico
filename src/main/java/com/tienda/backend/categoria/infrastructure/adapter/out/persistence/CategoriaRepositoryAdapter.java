package com.tienda.backend.categoria.infrastructure.adapter.out.persistence;

import com.tienda.backend.categoria.application.port.out.CategoriaRespositoryPort;
import com.tienda.backend.categoria.domain.model.Categoria;
import com.tienda.backend.categoria.application.model.CategoriaFiltro;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoriaRepositoryAdapter implements CategoriaRespositoryPort {
    private final CategoriaJpaRepository categoriaJpaRepository;
    private final CategoriaPersitenceMapper mapper;

    @Override
    public Categoria save(Categoria categoria) {
        CategoriaEntity guardado = categoriaJpaRepository.save(mapper.toEntity(categoria));
        return mapper.toDomain(guardado);
    }

    @Override
    public PageResult<Categoria> findAll(CategoriaFiltro filtro, int page, int size) {
        Page<CategoriaEntity> result = categoriaJpaRepository.findAll(CategoriaSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        return categoriaJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        categoriaJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return categoriaJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return categoriaJpaRepository.existsByNombreAndIdNot(nombre, id);
    }
}

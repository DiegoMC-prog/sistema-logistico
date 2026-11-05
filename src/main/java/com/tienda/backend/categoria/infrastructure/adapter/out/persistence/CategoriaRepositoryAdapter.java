package com.tienda.backend.categoria.infrastructure.adapter.out.persistence;

import com.tienda.backend.categoria.application.port.out.CategoriaRespositoryPort;
import com.tienda.backend.categoria.domain.model.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<Categoria> findAll() {
        return categoriaJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
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

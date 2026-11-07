package com.tienda.backend.unidadmedida.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.unidadmedida.application.model.UnidadMedidaFiltro;
import com.tienda.backend.unidadmedida.application.port.out.UnidadMedidaRepositoryPort;
import com.tienda.backend.unidadmedida.domain.model.UnidadMedida;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UnidadMedidaRepositoryAdapter implements UnidadMedidaRepositoryPort {
    private final UnidadMedidaJpaRepository unidadMedidaJpaRepository;
    private final UnidadMedidaPersistenceMapper mapper;

    @Override
    public UnidadMedida save(UnidadMedida unidadMedida) {
        UnidadMedidaEntity guardado = unidadMedidaJpaRepository.save(mapper.toEntity(unidadMedida));
        return mapper.toDomain(guardado);
    }

    @Override
    public PageResult<UnidadMedida> findAll(UnidadMedidaFiltro filtro, int page, int size) {
        Page<UnidadMedidaEntity> result = unidadMedidaJpaRepository.findAll(
                UnidadMedidaSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<UnidadMedida> findById(Long id) {
        return unidadMedidaJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        unidadMedidaJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return unidadMedidaJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return unidadMedidaJpaRepository.existsByNombreAndIdNot(nombre, id);
    }

    @Override
    public boolean existsByAbreviatura(String abreviatura) {
        return unidadMedidaJpaRepository.existsByAbreviatura(abreviatura);
    }

    @Override
    public boolean existsByAbreviaturaAndIdNot(String abreviatura, Long id) {
        return unidadMedidaJpaRepository.existsByAbreviaturaAndIdNot(abreviatura, id);
    }
}

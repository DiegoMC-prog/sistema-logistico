package com.tienda.backend.tipocliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.tipocliente.application.model.TipoClienteFiltro;
import com.tienda.backend.tipocliente.application.port.out.TipoClienteRepositoryPort;
import com.tienda.backend.tipocliente.domain.model.TipoCliente;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TipoClienteRepositoryAdapter implements TipoClienteRepositoryPort {
    private final TipoClienteJpaRepository tipoClienteJpaRepository;
    private final TipoClientePersistenceMapper mapper;

    @Override
    public TipoCliente save(TipoCliente tipoCliente) {
        return mapper.toDomain(tipoClienteJpaRepository.save(mapper.toEntity(tipoCliente)));
    }

    @Override
    public PageResult<TipoCliente> findAll(TipoClienteFiltro filtro, int page, int size) {
        Page<TipoClienteEntity> result = tipoClienteJpaRepository.findAll(
                TipoClienteSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<TipoCliente> findById(Long id) {
        return tipoClienteJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<TipoCliente> findAllById(Collection<Long> ids) {
        return tipoClienteJpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<TipoCliente> findByNombre(String nombre) {
        return tipoClienteJpaRepository.findByNombre(nombre).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        tipoClienteJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return tipoClienteJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return tipoClienteJpaRepository.existsByNombreAndIdNot(nombre, id);
    }
}

package com.tienda.backend.deposito.infrastructure.adapter.out.persistence;

import com.tienda.backend.deposito.application.model.DepositoFiltro;
import com.tienda.backend.deposito.application.port.out.DepositoRepositoryPort;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepositoRepositoryAdapter implements DepositoRepositoryPort {
    private final DepositoJpaRepository depositoJpaRepository;
    private final DepositoPersistenceMapper mapper;

    @Override
    public Deposito save(Deposito deposito) {
        DepositoEntity guardado = depositoJpaRepository.save(mapper.toEntity(deposito));
        return mapper.toDomain(guardado);
    }

    @Override
    public PageResult<Deposito> findAll(DepositoFiltro filtro, int page, int size) {
        Page<DepositoEntity> result = depositoJpaRepository.findAll(
                DepositoSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<Deposito> findById(Long id) {
        return depositoJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        depositoJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return depositoJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return depositoJpaRepository.existsByNombreAndIdNot(nombre, id);
    }
}

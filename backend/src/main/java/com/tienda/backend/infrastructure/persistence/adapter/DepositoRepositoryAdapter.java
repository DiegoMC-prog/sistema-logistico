package com.tienda.backend.infrastructure.persistence.adapter;

import com.tienda.backend.domain.model.Deposito;
import com.tienda.backend.domain.repository.DepositoRepository;
import com.tienda.backend.infrastructure.persistence.entity.DepositoEntity;
import com.tienda.backend.infrastructure.persistence.mapper.DepositoEntityMapper;
import com.tienda.backend.infrastructure.persistence.repository.DepositoJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DepositoRepositoryAdapter implements DepositoRepository {

    private final DepositoJpaRepository depositoJpaRepository;
    private final DepositoEntityMapper mapper;

    public DepositoRepositoryAdapter(DepositoJpaRepository depositoJpaRepository, DepositoEntityMapper mapper) {
        this.depositoJpaRepository = depositoJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Deposito guardar(Deposito deposito) {
        DepositoEntity entity = mapper.toEntity(deposito);
        DepositoEntity guardado = depositoJpaRepository.save(entity);
        return mapper.toDomain(guardado);
    }

    @Override
    public Optional<Deposito> buscarPorId(Long id) {
        return depositoJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<Deposito> listarTodos(Pageable pageable) {
        return depositoJpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Deposito> buscarPorNombre(String nombre, Pageable pageable) {
        return depositoJpaRepository.findByNombreContainingIgnoreCase(nombre, pageable).map(mapper::toDomain);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return depositoJpaRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public void eliminar(Long id) {
        depositoJpaRepository.deleteById(id);
    }

    @Override
    public long contarTotal() {
        return depositoJpaRepository.count();
    }

    @Override
    public long contarActivos() {
        return depositoJpaRepository.countByEstado(true);
    }

    @Override
    public long contarInactivos() {
        return depositoJpaRepository.countByEstado(false);
    }
}

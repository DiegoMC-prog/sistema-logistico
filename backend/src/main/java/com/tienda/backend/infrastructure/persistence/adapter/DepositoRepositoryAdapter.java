package com.tienda.backend.infrastructure.persistence.adapter;

import com.tienda.backend.domain.model.Deposito;
import com.tienda.backend.domain.repository.DepositoRepository;
import com.tienda.backend.infrastructure.persistence.entity.DepositoEntity;
import com.tienda.backend.infrastructure.persistence.mapper.DepositoEntityMapper;
import com.tienda.backend.infrastructure.persistence.repository.DepositoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DepositoRepositoryAdapter implements DepositoRepository {

    private final DepositoJpaRepository depositoJpaRepository;
    private final DepositoEntityMapper depositoEntityMapper;

    public DepositoRepositoryAdapter(DepositoJpaRepository depositoJpaRepository, DepositoEntityMapper depositoEntityMapper) {
        this.depositoJpaRepository = depositoJpaRepository;
        this.depositoEntityMapper = depositoEntityMapper;
    }

    @Override
    public Deposito guardar(Deposito deposito) {
        DepositoEntity entity = depositoEntityMapper.toEntity(deposito);
        DepositoEntity guardado = depositoJpaRepository.save(entity);

        return depositoEntityMapper.toDomain(guardado);
    }

    @Override
    public Optional<Deposito> buscarPorId(Long id) {
        return depositoJpaRepository.findById(id)
                .map(depositoEntityMapper::toDomain);
    }

    @Override
    public List<Deposito> listarTodas() {
        return depositoJpaRepository.findAll()
                .stream()
                .map(depositoEntityMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return depositoJpaRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public boolean existePorDescripcion(String descripcion) {
        return depositoJpaRepository.existsByDescripcionIgnoreCase(descripcion);
    }

    @Override
    public void eliminar(Long id) {
        depositoJpaRepository.deleteById(id);
    }

    @Override
    public boolean existePorId(Long id) {
        return depositoJpaRepository.existsById(id);
    }
}

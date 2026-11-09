package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.application.model.TurnoFiltro;
import com.tienda.backend.caja.application.port.out.TurnoRepositoryPort;
import com.tienda.backend.caja.domain.model.EstadoTurno;
import com.tienda.backend.caja.domain.model.TurnoCaja;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TurnoRepositoryAdapter implements TurnoRepositoryPort {
    private static final String ABIERTO = EstadoTurno.ABIERTO.name();
    private static final String ESPERA_MAXIMA_BLOQUEO = "5s";

    private final TurnoCajaJpaRepository turnoJpaRepository;
    private final CajaPersistenceMapper mapper;

    @Override
    public TurnoCaja save(TurnoCaja turno) {
        return mapper.toDomain(turnoJpaRepository.save(mapper.toEntity(turno)));
    }

    @Override
    public Optional<TurnoCaja> findById(Long id) {
        return turnoJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<TurnoCaja> findByIdParaActualizar(Long id) {
        turnoJpaRepository.configurarTiempoEsperaBloqueo(ESPERA_MAXIMA_BLOQUEO);
        return turnoJpaRepository.findParaActualizar(id).map(mapper::toDomain);
    }

    @Override
    public Optional<TurnoCaja> findByIdParaCompartir(Long id) {
        turnoJpaRepository.configurarTiempoEsperaBloqueo(ESPERA_MAXIMA_BLOQUEO);
        return turnoJpaRepository.findParaCompartir(id).map(mapper::toDomain);
    }

    @Override
    public Optional<TurnoCaja> findAbiertoByUsuarioId(Long usuarioId) {
        return turnoJpaRepository.findByUsuarioIdAndEstado(usuarioId, ABIERTO).map(mapper::toDomain);
    }

    @Override
    public Optional<TurnoCaja> findAbiertoByCajaId(Long cajaId) {
        return turnoJpaRepository.findByCajaIdAndEstado(cajaId, ABIERTO).map(mapper::toDomain);
    }

    @Override
    public boolean existsAbiertoByCajaId(Long cajaId) {
        return turnoJpaRepository.existsByCajaIdAndEstado(cajaId, ABIERTO);
    }

    @Override
    public boolean existsAbiertoByUsuarioId(Long usuarioId) {
        return turnoJpaRepository.existsByUsuarioIdAndEstado(usuarioId, ABIERTO);
    }

    @Override
    public boolean existsByCajaId(Long cajaId) {
        return turnoJpaRepository.existsByCajaId(cajaId);
    }

    @Override
    public PageResult<TurnoCaja> findAll(TurnoFiltro filtro, int page, int size) {
        Page<TurnoCajaEntity> result = turnoJpaRepository.findAll(TurnoCajaSpecifications.desde(filtro),
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("fechaApertura"), Sort.Order.desc("id"))));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}

package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TurnoCajaJpaRepository extends JpaRepository<TurnoCajaEntity, Long>, JpaSpecificationExecutor<TurnoCajaEntity> {
    Optional<TurnoCajaEntity> findByCajaIdAndEstado(Long cajaId, String estado);
    boolean existsByCajaIdAndEstado(Long cajaId, String estado);
    boolean existsByUsuarioIdAndEstado(Long usuarioId, String estado);
    boolean existsByCajaId(Long cajaId);
    Optional<TurnoCajaEntity> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    /** {@code SELECT ... FOR UPDATE}: quien lo pida mientras otra transacción tiene el turno espera a que termine. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TurnoCajaEntity t where t.id = :id")
    Optional<TurnoCajaEntity> findParaActualizar(@Param("id") Long id);

    /** {@code SELECT ... FOR SHARE}: no bloquea a otros lectores compartidos, pero sí a quien quiera escribir el turno. */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select t from TurnoCajaEntity t where t.id = :id")
    Optional<TurnoCajaEntity> findParaCompartir(@Param("id") Long id);

    /** Limita (solo dentro de la transacción actual) cuánto espera un bloqueo antes de fallar. */
    @Query(value = "select set_config('lock_timeout', :tiempo, true)", nativeQuery = true)
    String configurarTiempoEsperaBloqueo(@Param("tiempo") String tiempo);
}

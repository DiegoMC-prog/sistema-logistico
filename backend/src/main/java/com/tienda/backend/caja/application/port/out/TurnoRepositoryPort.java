package com.tienda.backend.caja.application.port.out;

import com.tienda.backend.caja.application.model.TurnoFiltro;
import com.tienda.backend.caja.domain.model.TurnoCaja;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface TurnoRepositoryPort {
    TurnoCaja save(TurnoCaja turno);
    Optional<TurnoCaja> findById(Long id);

    /**
     * Lee el turno bloqueándolo ({@code SELECT ... FOR UPDATE}) hasta el fin de la transacción: cerrar el turno y
     * registrar movimientos se serializan, así el efectivo esperado nunca se calcula sobre datos a medio cambiar.
     */
    Optional<TurnoCaja> findByIdParaActualizar(Long id);

    /**
     * Lee el turno con un bloqueo compartido ({@code SELECT ... FOR SHARE}): varias ventas del mismo turno avanzan a la
     * vez, pero el cierre (que lo bloquea para escribir) espera a que terminen y las ventas posteriores lo ven cerrado.
     */
    Optional<TurnoCaja> findByIdParaCompartir(Long id);

    Optional<TurnoCaja> findAbiertoByUsuarioId(Long usuarioId);
    Optional<TurnoCaja> findAbiertoByCajaId(Long cajaId);
    boolean existsAbiertoByCajaId(Long cajaId);
    boolean existsAbiertoByUsuarioId(Long usuarioId);
    boolean existsByCajaId(Long cajaId);
    PageResult<TurnoCaja> findAll(TurnoFiltro filtro, int page, int size);
}

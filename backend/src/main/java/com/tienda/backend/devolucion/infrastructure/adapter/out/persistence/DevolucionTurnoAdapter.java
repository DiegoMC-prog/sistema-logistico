package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.application.model.TurnoRef;
import com.tienda.backend.devolucion.application.port.out.TurnoDevolucionPort;
import com.tienda.backend.caja.application.port.in.ConsultarEfectivoTurnoUseCase;
import com.tienda.backend.caja.application.port.in.RegistrarMovimientoCajaUseCase;
import com.tienda.backend.caja.application.port.out.TurnoRepositoryPort;
import com.tienda.backend.caja.domain.model.TipoMovimientoCaja;
import com.tienda.backend.caja.domain.model.TurnoCaja;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DevolucionTurnoAdapter implements TurnoDevolucionPort {
    private final TurnoRepositoryPort turnoRepositoryPort;
    private final ConsultarEfectivoTurnoUseCase consultarEfectivoTurnoUseCase;
    private final RegistrarMovimientoCajaUseCase registrarMovimientoCajaUseCase;

    @Override
    public Optional<TurnoRef> turnoAbiertoDeUsuario(Long usuarioId) {
        return turnoRepositoryPort.findAbiertoByUsuarioId(usuarioId).map(this::aRef);
    }

    @Override
    public Optional<TurnoRef> bloquearParaEscritura(Long turnoId) {
        return turnoRepositoryPort.findByIdParaActualizar(turnoId).map(this::aRef);
    }

    @Override
    public BigDecimal efectivoDisponible(Long turnoId) {
        return consultarEfectivoTurnoUseCase.efectivoDisponible(turnoId);
    }

    @Override
    public void registrarEgreso(Long turnoId, BigDecimal monto, String motivo) {
        registrarMovimientoCajaUseCase.registrar(turnoId, TipoMovimientoCaja.EGRESO, monto, motivo);
    }

    private TurnoRef aRef(TurnoCaja t) {
        return new TurnoRef(t.getId(), t.estaAbierto());
    }
}

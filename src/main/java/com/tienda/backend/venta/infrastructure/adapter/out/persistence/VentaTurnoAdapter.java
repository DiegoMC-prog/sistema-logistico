package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.model.TurnoVenta;
import com.tienda.backend.venta.application.port.out.TurnoVentaPort;
import com.tienda.backend.caja.application.port.out.CajaRepositoryPort;
import com.tienda.backend.caja.application.port.out.TurnoRepositoryPort;
import com.tienda.backend.caja.domain.model.TurnoCaja;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VentaTurnoAdapter implements TurnoVentaPort {
    private final TurnoRepositoryPort turnoRepositoryPort;
    private final CajaRepositoryPort cajaRepositoryPort;

    @Override
    public Optional<TurnoVenta> turnoAbiertoDeUsuario(Long usuarioId) {
        return turnoRepositoryPort.findAbiertoByUsuarioId(usuarioId).flatMap(this::aTurnoVenta);
    }

    @Override
    public Optional<TurnoVenta> bloquearCompartido(Long turnoId) {
        return turnoRepositoryPort.findByIdParaCompartir(turnoId).flatMap(this::aTurnoVenta);
    }

    private Optional<TurnoVenta> aTurnoVenta(TurnoCaja turno) {
        return cajaRepositoryPort.findById(turno.getCajaId())
                .map(caja -> new TurnoVenta(turno.getId(), caja.getId(), caja.getDepositoId(), turno.estaAbierto()));
    }
}

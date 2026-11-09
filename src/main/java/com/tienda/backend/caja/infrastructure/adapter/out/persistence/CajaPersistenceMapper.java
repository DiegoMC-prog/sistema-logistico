package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.domain.model.Caja;
import com.tienda.backend.caja.domain.model.EstadoCaja;
import com.tienda.backend.caja.domain.model.EstadoTurno;
import com.tienda.backend.caja.domain.model.MovimientoCaja;
import com.tienda.backend.caja.domain.model.TipoMovimientoCaja;
import com.tienda.backend.caja.domain.model.TurnoCaja;
import org.springframework.stereotype.Component;

@Component
public class CajaPersistenceMapper {
    public Caja toDomain(CajaEntity e) {
        return new Caja(e.getId(), e.getNombre(), e.getDepositoId(), EstadoCaja.valueOf(e.getEstado()));
    }

    public CajaEntity toEntity(Caja d) {
        return CajaEntity.builder().id(d.getId()).nombre(d.getNombre()).depositoId(d.getDepositoId())
                .estado(d.getEstado().name()).build();
    }

    public TurnoCaja toDomain(TurnoCajaEntity e) {
        return new TurnoCaja(e.getId(), e.getCajaId(), e.getUsuarioId(), e.getFechaApertura(), e.getMontoApertura(),
                e.getFechaCierre(), e.getMontoCierreDeclarado(), e.getMontoCierreEsperado(),
                EstadoTurno.valueOf(e.getEstado()));
    }

    public TurnoCajaEntity toEntity(TurnoCaja d) {
        return TurnoCajaEntity.builder().id(d.getId()).cajaId(d.getCajaId()).usuarioId(d.getUsuarioId())
                .fechaApertura(d.getFechaApertura()).montoApertura(d.getMontoApertura())
                .fechaCierre(d.getFechaCierre()).montoCierreDeclarado(d.getMontoCierreDeclarado())
                .montoCierreEsperado(d.getMontoCierreEsperado()).estado(d.getEstado().name()).build();
    }

    public MovimientoCaja toDomain(MovimientoCajaEntity e) {
        return new MovimientoCaja(e.getId(), e.getTurnoId(), e.getUsuarioId(), TipoMovimientoCaja.valueOf(e.getTipo()),
                e.getMonto(), e.getMotivo(), e.getFecha());
    }

    public MovimientoCajaEntity toEntity(MovimientoCaja d) {
        return MovimientoCajaEntity.builder().id(d.getId()).turnoId(d.getTurnoId()).usuarioId(d.getUsuarioId())
                .tipo(d.getTipo().name()).monto(d.getMonto()).motivo(d.getMotivo()).fecha(d.getFecha()).build();
    }
}

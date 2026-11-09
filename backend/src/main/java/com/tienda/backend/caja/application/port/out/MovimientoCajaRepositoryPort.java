package com.tienda.backend.caja.application.port.out;

import com.tienda.backend.caja.application.model.TotalesMovimientos;
import com.tienda.backend.caja.domain.model.MovimientoCaja;
import com.tienda.backend.shared.application.model.PageResult;

public interface MovimientoCajaRepositoryPort {
    MovimientoCaja save(MovimientoCaja movimiento);
    PageResult<MovimientoCaja> findByTurnoId(Long turnoId, int page, int size);
    TotalesMovimientos totalesPorTurno(Long turnoId);
}

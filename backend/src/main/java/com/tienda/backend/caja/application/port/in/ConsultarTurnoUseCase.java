package com.tienda.backend.caja.application.port.in;

import com.tienda.backend.caja.application.model.TurnoDetalle;
import com.tienda.backend.caja.application.model.TurnoFiltro;
import com.tienda.backend.caja.application.model.TurnoResumen;
import com.tienda.backend.caja.domain.model.MovimientoCaja;
import com.tienda.backend.shared.application.model.PageResult;

public interface ConsultarTurnoUseCase {
    TurnoResumen obtener(Long turnoId);
    TurnoResumen turnoActual(Long cajaId);
    PageResult<TurnoDetalle> listar(TurnoFiltro filtro, int page, int size);
    PageResult<MovimientoCaja> movimientos(Long turnoId, int page, int size);
}

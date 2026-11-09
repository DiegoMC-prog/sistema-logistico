package com.tienda.backend.caja.application.port.in;

import com.tienda.backend.caja.application.model.TurnoResumen;
import java.math.BigDecimal;

public interface CerrarTurnoUseCase {
    TurnoResumen cerrar(Long turnoId, BigDecimal montoDeclarado);
}

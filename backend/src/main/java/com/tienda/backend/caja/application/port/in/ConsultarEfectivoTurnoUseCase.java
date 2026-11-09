package com.tienda.backend.caja.application.port.in;

import java.math.BigDecimal;

public interface ConsultarEfectivoTurnoUseCase {
    /** Efectivo que debería haber ahora en la caja del turno (la misma fórmula del efectivo esperado al cerrar). */
    BigDecimal efectivoDisponible(Long turnoId);
}

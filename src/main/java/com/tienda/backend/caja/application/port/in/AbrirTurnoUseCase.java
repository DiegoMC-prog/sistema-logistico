package com.tienda.backend.caja.application.port.in;

import com.tienda.backend.caja.application.model.TurnoResumen;
import java.math.BigDecimal;

public interface AbrirTurnoUseCase {
    TurnoResumen abrir(Long cajaId, BigDecimal montoApertura);
}

package com.tienda.backend.caja.application.port.in;

import com.tienda.backend.caja.domain.model.MovimientoCaja;
import com.tienda.backend.caja.domain.model.TipoMovimientoCaja;
import java.math.BigDecimal;

public interface RegistrarMovimientoCajaUseCase {
    MovimientoCaja registrar(Long turnoId, TipoMovimientoCaja tipo, BigDecimal monto, String motivo);
}

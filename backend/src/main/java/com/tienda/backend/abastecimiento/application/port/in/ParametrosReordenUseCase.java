package com.tienda.backend.abastecimiento.application.port.in;

import com.tienda.backend.abastecimiento.application.model.GuardarParametrosComando;
import com.tienda.backend.abastecimiento.application.model.ParametrosProducto;

public interface ParametrosReordenUseCase {
    ParametrosProducto obtenerParametros(Long productoId);

    ParametrosProducto guardarParametros(Long productoId, GuardarParametrosComando comando);
}

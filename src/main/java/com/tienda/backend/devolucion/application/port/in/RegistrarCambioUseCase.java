package com.tienda.backend.devolucion.application.port.in;

import com.tienda.backend.devolucion.application.model.RegistrarCambioComando;
import com.tienda.backend.devolucion.application.model.ResultadoRegistroDevolucion;

public interface RegistrarCambioUseCase {
    ResultadoRegistroDevolucion registrarCambio(RegistrarCambioComando comando);
}

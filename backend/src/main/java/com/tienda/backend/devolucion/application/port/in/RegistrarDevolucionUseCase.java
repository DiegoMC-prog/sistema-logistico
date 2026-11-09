package com.tienda.backend.devolucion.application.port.in;

import com.tienda.backend.devolucion.application.model.RegistrarDevolucionComando;
import com.tienda.backend.devolucion.application.model.ResultadoRegistroDevolucion;

public interface RegistrarDevolucionUseCase {
    ResultadoRegistroDevolucion registrar(RegistrarDevolucionComando comando);
}

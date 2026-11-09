package com.tienda.backend.credito.application.port.in;

import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.RegistrarAbonoComando;

public interface RegistrarAbonoUseCase {
    AbonoResumen registrar(RegistrarAbonoComando comando);
}

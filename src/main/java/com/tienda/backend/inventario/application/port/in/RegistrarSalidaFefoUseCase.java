package com.tienda.backend.inventario.application.port.in;

import com.tienda.backend.inventario.application.model.ResultadoSalidaFefo;
import com.tienda.backend.inventario.application.model.SalidaFefoComando;

public interface RegistrarSalidaFefoUseCase {
    ResultadoSalidaFefo registrarSalidaFefo(SalidaFefoComando comando);
}

package com.tienda.backend.reporte.application.port.in;

import com.tienda.backend.reporte.application.model.Panel;

public interface ConsultarPanelUseCase {
    /** El panel del usuario autenticado: solo con los bloques cuyo reporte puede ver. */
    Panel consultar();
}

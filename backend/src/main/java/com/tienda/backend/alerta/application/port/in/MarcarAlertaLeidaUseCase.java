package com.tienda.backend.alerta.application.port.in;

import com.tienda.backend.alerta.application.model.AlertaResumen;

public interface MarcarAlertaLeidaUseCase {
    AlertaResumen marcarLeida(Long alertaId);

    /** Marca como leídas todas las alertas activas; devuelve cuántas cambió. */
    int marcarTodasLeidas();
}

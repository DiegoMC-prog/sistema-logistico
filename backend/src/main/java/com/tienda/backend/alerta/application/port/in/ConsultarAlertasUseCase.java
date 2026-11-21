package com.tienda.backend.alerta.application.port.in;

import com.tienda.backend.alerta.application.model.AlertaFiltro;
import com.tienda.backend.alerta.application.model.AlertaResumen;
import com.tienda.backend.alerta.application.model.ConteoAlertas;
import com.tienda.backend.shared.application.model.PageResult;

public interface ConsultarAlertasUseCase {
    PageResult<AlertaResumen> listar(AlertaFiltro filtro, int page, int size);

    ConteoAlertas conteo();
}

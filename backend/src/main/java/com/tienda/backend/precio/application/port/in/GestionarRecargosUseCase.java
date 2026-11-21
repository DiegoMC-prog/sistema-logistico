package com.tienda.backend.precio.application.port.in;

import com.tienda.backend.precio.application.model.CrearRecargoComando;
import com.tienda.backend.precio.application.model.RecargoFiltro;
import com.tienda.backend.precio.application.model.RecargoResumen;
import com.tienda.backend.shared.application.model.PageResult;

/** Recargos por escasez (RF-35): los activa y desactiva el administrador; nunca se borran. */
public interface GestionarRecargosUseCase {

    PageResult<RecargoResumen> listar(RecargoFiltro filtro, int page, int size);

    RecargoResumen crear(CrearRecargoComando comando);

    RecargoResumen desactivar(Long id);
}

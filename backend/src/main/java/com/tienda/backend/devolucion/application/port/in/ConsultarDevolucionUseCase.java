package com.tienda.backend.devolucion.application.port.in;

import com.tienda.backend.devolucion.application.model.DevolucionFiltro;
import com.tienda.backend.devolucion.application.model.DevolucionVista;
import com.tienda.backend.devolucion.domain.model.Devolucion;
import com.tienda.backend.shared.application.model.PageResult;

public interface ConsultarDevolucionUseCase {
    DevolucionVista obtener(Long id);
    PageResult<Devolucion> listar(DevolucionFiltro filtro, int page, int size);
}

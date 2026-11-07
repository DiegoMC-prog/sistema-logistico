package com.tienda.backend.presentacion.application.port.in;

import com.tienda.backend.presentacion.application.model.EditarPresentacionComando;
import com.tienda.backend.presentacion.application.model.PresentacionDetalle;

public interface EditarPresentacionUseCase {
    PresentacionDetalle editar(Long id, EditarPresentacionComando comando);
}

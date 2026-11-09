package com.tienda.backend.auth.application.port.in;

import com.tienda.backend.auth.application.model.DatosAcceso;

public interface ObtenerPerfilUseCase {
    DatosAcceso obtenerPerfil(Long usuarioId);
}

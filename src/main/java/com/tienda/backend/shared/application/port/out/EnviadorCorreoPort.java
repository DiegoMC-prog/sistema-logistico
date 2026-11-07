package com.tienda.backend.shared.application.port.out;

import com.tienda.backend.shared.application.model.CorreoMensaje;

/** Servicio único de correo del sistema; los módulos solo indican plantilla y datos. */
public interface EnviadorCorreoPort {
    /**
     * @throws com.tienda.backend.shared.domain.exception.CorreoNoEnviadoException si el servidor de correo rechaza el envío
     */
    void enviar(CorreoMensaje mensaje);
}

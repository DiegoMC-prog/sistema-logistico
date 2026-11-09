package com.tienda.backend.auth.application.port.out;

import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.model.OrigenCambioPassword;

public interface NotificadorCambioPasswordPort {
    /**
     * Avisa al dueño de la cuenta, por correo, que su contraseña acaba de cambiar. Si no fue él, es la señal de que
     * alguien más tiene acceso. Es asíncrono y no falla: un error de envío solo se registra, nunca revierte el cambio.
     */
    void avisarCambio(ContactoUsuario destinatario, OrigenCambioPassword origen);
}

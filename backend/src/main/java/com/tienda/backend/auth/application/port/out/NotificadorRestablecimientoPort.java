package com.tienda.backend.auth.application.port.out;

import com.tienda.backend.auth.application.model.ContactoUsuario;

public interface NotificadorRestablecimientoPort {
    /**
     * Envía al usuario el correo con el enlace para restablecer su contraseña. Es asíncrono: la petición no espera al
     * servidor de correo, así tarda lo mismo exista o no la cuenta, y un fallo de envío no se le muestra a quien pidió.
     *
     * @param token          token en claro (solo viaja en el enlace del correo)
     * @param minutosValidez cuánto dura el enlace, para decírselo al usuario
     */
    void enviarEnlace(ContactoUsuario destinatario, String token, int minutosValidez);
}

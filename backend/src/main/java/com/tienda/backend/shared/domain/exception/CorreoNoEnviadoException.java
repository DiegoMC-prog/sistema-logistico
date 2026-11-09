package com.tienda.backend.shared.domain.exception;

public class CorreoNoEnviadoException extends ServicioExternoException {
    public CorreoNoEnviadoException(String destinatario) {
        super("No se pudo enviar el correo a " + destinatario
                + ". Verifique el servidor de correo e intente nuevamente.");
    }
}

package com.tienda.backend.usuario.application.port.out;

public interface PasswordGeneratorPort {
    /** Contraseña temporal aleatoria y segura, en claro (solo se usa para enviarla y hashearla). */
    String generar();
}

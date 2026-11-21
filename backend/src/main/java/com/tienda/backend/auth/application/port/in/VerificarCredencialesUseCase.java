package com.tienda.backend.auth.application.port.in;

/**
 * Verifica usuario (o correo) y contraseña aplicando el bloqueo por intentos fallidos (R06). Lo usan el inicio de
 * sesión y la autorización de un administrador sobre el monto umbral (R08).
 */
public interface VerificarCredencialesUseCase {

    /**
     * @return id del usuario verificado
     * @throws com.tienda.backend.auth.domain.exception.CredencialesInvalidasException si no coinciden o está inactivo
     * @throws com.tienda.backend.auth.domain.exception.CuentaBloqueadaException       si la cuenta está bloqueada
     */
    Long verificar(String identificador, String password);
}

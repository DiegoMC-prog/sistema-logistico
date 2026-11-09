package com.tienda.backend.auth.application.port.in;

public interface SolicitarRestablecimientoUseCase {
    /**
     * "Olvidé mi contraseña": si el usuario existe y está activo se le envía un enlace de un solo uso. Termina igual
     * exista o no la cuenta (y aunque se haya alcanzado el límite de su cuenta), para no revelar qué usuarios existen.
     *
     * @param identificador usuario o correo electrónico
     * @param ip            origen de la petición, para limitar el abuso
     * @throws com.tienda.backend.shared.domain.exception.DemasiadasSolicitudesException si esa IP superó su límite
     */
    void solicitar(String identificador, String ip);
}

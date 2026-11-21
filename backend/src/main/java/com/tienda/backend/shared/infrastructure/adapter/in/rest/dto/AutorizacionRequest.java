package com.tienda.backend.shared.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.shared.application.model.CredencialesSupervisor;
import jakarta.validation.constraints.Size;

/** R08: credenciales del administrador que autoriza una operación sobre el monto umbral (opcional en el body). */
public record AutorizacionRequest(
        @Size(max = 255, message = "El usuario del administrador es demasiado largo")
        String username,

        @Size(max = 255, message = "La contraseña del administrador es demasiado larga")
        String password
) {
    public CredencialesSupervisor toCredenciales() {
        return new CredencialesSupervisor(username, password);
    }

    public static CredencialesSupervisor credenciales(AutorizacionRequest request) {
        return request == null ? null : request.toCredenciales();
    }

    @Override
    public String toString() {
        return "AutorizacionRequest[username=" + username + "]";
    }
}

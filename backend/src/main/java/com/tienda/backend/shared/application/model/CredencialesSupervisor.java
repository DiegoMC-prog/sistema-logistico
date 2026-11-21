package com.tienda.backend.shared.application.model;

/** Usuario (o correo) y contraseña del administrador que autoriza una operación sobre el monto umbral (R08). */
public record CredencialesSupervisor(String username, String password) {

    /** Las credenciales vacías equivalen a no enviarlas. */
    public boolean vacias() {
        return username == null || username.isBlank() || password == null || password.isEmpty();
    }

    @Override
    public String toString() {
        // Que la contraseña no termine en un log por accidente
        return "CredencialesSupervisor[username=" + username + "]";
    }
}

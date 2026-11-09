package com.tienda.backend.auth.application.port.out;

import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.model.CredencialesUsuario;
import com.tienda.backend.auth.application.model.DatosAcceso;

import java.util.List;
import java.util.Optional;

/** Acceso del módulo de autenticación a los datos de usuarios, roles y permisos. */
public interface UsuarioAutenticacionPort {
    Optional<CredencialesUsuario> buscarCredencialesPorUsername(String username);

    Optional<CredencialesUsuario> buscarCredencialesPorId(Long usuarioId);

    /** Busca por nombre de usuario o, si no hay, por correo electrónico (sin distinguir mayúsculas). */
    Optional<ContactoUsuario> buscarContactoPorIdentificador(String identificador);

    Optional<ContactoUsuario> buscarContactoPorId(Long usuarioId);

    /** Roles y permisos efectivos leídos de la BD en este momento. */
    Optional<DatosAcceso> cargarDatosAcceso(Long usuarioId);

    /** Guarda la contraseña ya hasheada y deja de considerarla temporal. */
    void guardarPassword(Long usuarioId, String passwordHash);

    List<Long> usuarioIdsPorRol(Long rolId);
}

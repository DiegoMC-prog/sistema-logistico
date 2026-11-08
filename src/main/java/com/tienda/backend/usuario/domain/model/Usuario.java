package com.tienda.backend.usuario.domain.model;

import com.tienda.backend.usuario.domain.exception.UsuarioDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;
import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {
    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9._-]{3,50}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int NOMBRE_MAX = 150;
    private static final int EMAIL_MAX = 255;

    @EqualsAndHashCode.Include
    private final Long id;

    private String username;
    private String nombre;
    private String email;
    private EstadoUsuario estado;
    /** Contraseña ya hasheada. Nunca sale del módulo en una respuesta. */
    private String passwordHash;
    /** true mientras la contraseña sea la temporal generada por el sistema (el usuario debe cambiarla al ingresar). */
    private boolean passwordTemporal;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public Usuario(Long id, String username, String nombre, String email,
                   EstadoUsuario estado, String passwordHash, boolean passwordTemporal) {
        this.id = id;
        setUsername(username);
        setNombre(nombre);
        setEmail(email);
        setEstado(estado);
        setPasswordHash(passwordHash);
        this.passwordTemporal = passwordTemporal;
    }

    /** Usuario nuevo: nace ACTIVO, sin id y con la contraseña temporal generada por el sistema. */
    public Usuario(String username, String nombre, String email, String passwordHashTemporal) {
        this(null, username, nombre, email, EstadoUsuario.ACTIVO, passwordHashTemporal, true);
    }

    /** El username es inmutable. */
    public void editar(String nombre, String email) {
        setNombre(nombre);
        setEmail(email);
    }

    /** El propio usuario eligió su contraseña: deja de ser temporal. */
    public void cambiarPassword(String nuevoPasswordHash) {
        setPasswordHash(nuevoPasswordHash);
        this.passwordTemporal = false;
    }

    /** Reemplaza la contraseña por una temporal generada por el sistema. */
    public void asignarPasswordTemporal(String passwordHashTemporal) {
        setPasswordHash(passwordHashTemporal);
        this.passwordTemporal = true;
    }

    public void activar() {
        this.estado = EstadoUsuario.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoUsuario.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    private void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new UsuarioDatosInvalidosException("El username es obligatorio y no puede estar vacío");
        }
        String limpio = username.trim().toLowerCase(Locale.ROOT);
        if (!USERNAME.matcher(limpio).matches()) {
            throw new UsuarioDatosInvalidosException(
                    "El username debe tener entre 3 y 50 caracteres y solo puede contener letras, números, punto, guion y guion bajo");
        }
        this.username = limpio;
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new UsuarioDatosInvalidosException("El nombre es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > NOMBRE_MAX) {
            throw new UsuarioDatosInvalidosException("El nombre no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UsuarioDatosInvalidosException("El email es obligatorio y no puede estar vacío");
        }
        String limpio = email.trim().toLowerCase(Locale.ROOT);
        if (limpio.length() > EMAIL_MAX || !EMAIL.matcher(limpio).matches()) {
            throw new UsuarioDatosInvalidosException("El email no tiene un formato válido");
        }
        this.email = limpio;
    }

    private void setEstado(EstadoUsuario estado) {
        if (estado == null) {
            throw new UsuarioDatosInvalidosException("El estado del usuario es obligatorio");
        }
        this.estado = estado;
    }

    private void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new UsuarioDatosInvalidosException("La contraseña es obligatoria");
        }
        this.passwordHash = passwordHash;
    }
}

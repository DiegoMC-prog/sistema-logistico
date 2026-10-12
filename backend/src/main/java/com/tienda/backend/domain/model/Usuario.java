package com.tienda.backend.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @EqualsAndHashCode.Include
    private final Long id;

    private String username;
    private String email;
    private String password;
    private String nombre;
    private String apellido;
    private Rol rol;
    private Boolean estado;

    public Usuario(Long id, String username, String email, String password,
                   String nombre, String apellido, Rol rol, Boolean estado) {
        validarUsername(username);
        validarEmail(email);
        validarPassword(password);
        validarNombre(nombre, "nombre");
        validarNombre(apellido, "apellido");
        validarRol(rol);

        this.id = id;
        this.username = username.trim().toLowerCase();
        this.email = email.trim().toLowerCase();
        this.password = password;
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.rol = rol;
        this.estado = estado != null ? estado : Boolean.TRUE;
    }

    public static Usuario crear(String username, String email, String passwordHasheada,
                                String nombre, String apellido, Rol rol) {
        return new Usuario(null, username, email, passwordHasheada, nombre, apellido, rol, true);
    }

    public void actualizarPerfil(String nuevoNombre, String nuevoApellido, String nuevoEmail) {
        validarNombre(nuevoNombre, "nombre");
        validarNombre(nuevoApellido, "apellido");
        validarEmail(nuevoEmail);

        this.nombre = nuevoNombre.trim();
        this.apellido = nuevoApellido.trim();
        this.email = nuevoEmail.trim().toLowerCase();
    }

    public void cambiarPassword(String nuevaPasswordHasheada) {
        validarPassword(nuevaPasswordHasheada);
        this.password = nuevaPasswordHasheada;
    }

    public void cambiarRol(Rol nuevoRol) {
        validarRol(nuevoRol);
        this.rol = nuevoRol;
    }

    public void activar() {
        this.estado = true;
    }

    public void desactivar() {
        this.estado = false;
    }

    public void cambiarEstado(boolean nuevoEstado) {
        this.estado = nuevoEstado;
    }

    private void validarUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 50 caracteres.");
        }
    }

    private void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido.");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("El correo electrónico no puede superar los 100 caracteres.");
        }
    }

    private void validarPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }
    }

    private void validarNombre(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El " + campo + " es obligatorio.");
        }
        if (valor.length() > 100) {
            throw new IllegalArgumentException("El " + campo + " no puede superar los 100 caracteres.");
        }
    }

    private void validarRol(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol del usuario es obligatorio.");
        }
    }
}

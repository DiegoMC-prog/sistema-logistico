package com.tienda.backend.rol.domain.model;

import com.tienda.backend.rol.domain.exception.RolDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Rol {
    public static final String NOMBRE_ADMIN = "ADMIN";
    private static final int NOMBRE_MAX = 150;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;

    public Rol(Long id, String nombre) {
        this.id = id;
        setNombre(nombre);
    }

    public Rol(String nombre) {
        this(null, nombre);
    }

    public void editarNombre(String nuevoNombre) {
        setNombre(nuevoNombre);
    }

    /** El rol ADMIN es de sistema: no se renombra, no se elimina y conserva todos sus permisos. */
    public boolean esDeSistema() {
        return NOMBRE_ADMIN.equals(nombre);
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new RolDatosInvalidosException("El nombre del rol es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim().toUpperCase(Locale.ROOT);
        if (limpio.length() > NOMBRE_MAX) {
            throw new RolDatosInvalidosException("El nombre del rol no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }
}

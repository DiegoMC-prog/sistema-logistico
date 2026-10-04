package com.tienda.backend.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Deposito {

    @EqualsAndHashCode.Include
    private final Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Deposito(String nombre, String descripcion) {
        this(null, nombre, descripcion, true, null, null);
    }

    public Deposito(Long id, String nombre, String descripcion, Boolean activo, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nombre = validarNombre(nombre);
        this.descripcion = validarDescripcion(descripcion);
        this.activo = activo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /*
        --reviar esta logica (DM)
    public void actualizarNombre(String nuevoNombre) {
        this.nombre = validarNombre(nuevoNombre);
        this.updatedAt = LocalDateTime.now();
    }

    public void actualizarDescripcion(String nuevaDescripcion) {
        this.descripcion = validarDescripcion(nuevaDescripcion);
        this.updatedAt = LocalDateTime.now();
    }
    */

    public void actualizarDatos(String nuevoNombre, String nuevaDescripcion, Boolean activo) {
        this.nombre = validarNombre(nuevoNombre);
        this.descripcion = validarDescripcion(nuevaDescripcion);
        this.activo = activo;
        this.updatedAt = LocalDateTime.now();
    }

    public void cambiarEstado() {
        this.activo = !this.activo;
        this.updatedAt = LocalDateTime.now();
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del deposito no puede estar vacio");
        }

        if (nombre.length() > 150) {
            throw new IllegalArgumentException("El nombre no puede tener mas de 150 caracteres");
        }

        return nombre.trim();
    }

    private String validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return null;
        }

        String descripcionLimpia = descripcion.trim();
        if (descripcionLimpia.length() > 500) {
            throw new IllegalArgumentException("La descripcion no puede tener mas de 500 caracteres");
        }

        return descripcionLimpia;
    }
}

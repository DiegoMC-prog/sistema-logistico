package com.tienda.backend.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Deposito {

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private String descripcion;
    private Boolean estado;

    public Deposito(Long id, String nombre, String descripcion, Boolean estado) {
        validarNombre(nombre);
        validarDescripcion(descripcion);

        this.id = id;
        this.nombre = nombre.trim();
        this.descripcion = descripcion != null ? descripcion.trim() : null;
        this.estado = estado != null ? estado : Boolean.TRUE;
    }

    public static Deposito crear(String nombre, String descripcion) {
        return new Deposito(null, nombre, descripcion, true);
    }

    public void actualizar(String nuevoNombre, String nuevaDescripcion) {
        validarNombre(nuevoNombre);
        validarDescripcion(nuevaDescripcion);
        this.nombre = nuevoNombre.trim();
        this.descripcion = nuevaDescripcion != null ? nuevaDescripcion.trim() : null;
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

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del depósito es obligatorio y no puede estar vacío.");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre del depósito no puede superar los 100 caracteres.");
        }
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion != null && descripcion.length() > 200) {
            throw new IllegalArgumentException("La descripción del depósito no puede superar los 200 caracteres.");
        }
    }
}

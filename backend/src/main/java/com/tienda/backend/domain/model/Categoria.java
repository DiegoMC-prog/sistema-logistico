package com.tienda.backend.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Categoria {

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private Long totalProductos;

    public Categoria(Long id, String nombre) {
        this(id, nombre, 0L);
    }

    public Categoria(Long id, String nombre, Long totalProductos) {
        this.id = id;
        this.nombre = nombre;
        this.totalProductos = totalProductos != null ? totalProductos : 0L;
    }

    public static Categoria crear(String nombre) {
        return new Categoria(null, nombre, 0L);
    }

    public void actualizar(String nuevoNombre) {
        validarNombre(nuevoNombre);
        this.nombre = nuevoNombre.trim();
    }

    public void asignarTotalProductos(Long totalProductos) {
        this.totalProductos = totalProductos != null ? totalProductos : 0L;
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio y no puede estar vacío.");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre de la categoría no puede superar los 100 caracteres.");
        }
    }
}

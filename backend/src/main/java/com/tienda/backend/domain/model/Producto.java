package com.tienda.backend.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    @EqualsAndHashCode.Include
    private final Long id;

    private String codigoBarras;
    private String nombre;
    private String descripcion;
    private String imagen;
    private Categoria categoria;
    private Boolean estado;

    public Producto(Long id, String codigoBarras, String nombre, String descripcion, String imagen, Categoria categoria, Boolean estado) {
        validarCodigoBarras(codigoBarras);
        validarNombre(nombre);
        validarDescripcion(descripcion);
        validarCategoria(categoria);

        this.id = id;
        this.codigoBarras = codigoBarras.trim();
        this.nombre = nombre.trim();
        this.descripcion = descripcion != null ? descripcion.trim() : null;
        this.imagen = imagen != null ? imagen.trim() : null;
        this.categoria = categoria;
        this.estado = estado != null ? estado : Boolean.TRUE;
    }

    public static Producto crear(String codigoBarras, String nombre, String descripcion, String imagen, Categoria categoria) {
        return new Producto(null, codigoBarras, nombre, descripcion, imagen, categoria, true);
    }

    public void actualizar(String nuevoCodigoBarras, String nuevoNombre, String nuevaDescripcion, String nuevaImagen, Categoria nuevaCategoria) {
        validarCodigoBarras(nuevoCodigoBarras);
        validarNombre(nuevoNombre);
        validarDescripcion(nuevaDescripcion);
        validarCategoria(nuevaCategoria);

        this.codigoBarras = nuevoCodigoBarras.trim();
        this.nombre = nuevoNombre.trim();
        this.descripcion = nuevaDescripcion != null ? nuevaDescripcion.trim() : null;
        this.imagen = nuevaImagen != null ? nuevaImagen.trim() : null;
        this.categoria = nuevaCategoria;
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

    private void validarCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            throw new IllegalArgumentException("El código de barras del producto es obligatorio y no puede estar vacío.");
        }
        if (codigoBarras.length() > 100) {
            throw new IllegalArgumentException("El código de barras no puede superar los 100 caracteres.");
        }
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio y no puede estar vacío.");
        }
        if (nombre.length() > 200) {
            throw new IllegalArgumentException("El nombre del producto no puede superar los 200 caracteres.");
        }
    }

    private void validarDescripcion(String descripcion) {
        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException("La descripción del producto no puede superar los 500 caracteres.");
        }
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría asociada al producto es obligatoria.");
        }
        if (categoria.getId() == null) {
            throw new IllegalArgumentException("La categoría asociada debe tener un ID válido.");
        }
    }
}

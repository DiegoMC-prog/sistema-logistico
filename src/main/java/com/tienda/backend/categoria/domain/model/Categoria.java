package com.tienda.backend.categoria.domain.model;

import com.tienda.backend.categoria.domain.exception.CategoriaNombreInvalidoException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Categoria {
    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;

    public Categoria(Long id, String nombre) {
        this.id = id;
        setNombreValido(nombre);
    }

    public Categoria(String nombre) {
        this(null, nombre);
    }

    public void editarNombre(String nuevoNombre) {
        setNombreValido(nuevoNombre);
    }

    private void setNombreValido(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new CategoriaNombreInvalidoException();
        }
        this.nombre = nombre.trim();
    }
}

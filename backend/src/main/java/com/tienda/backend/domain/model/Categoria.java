package com.tienda.backend.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Categoria {

    @EqualsAndHashCode.Include
    private final Long id;
    private String nombre;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor para creación
    public Categoria(String nombre) {
        this(null, nombre, null, null);
    }

    // Constructor completo (usado para reconstruir desde persistencia)
    public Categoria(Long id, String nombre, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nombre = validarNombre(nombre);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // --- Reglas de negocio ---

    public void actualizarNombre(String nuevoNombre) {
        this.nombre = validarNombre(nuevoNombre);
        this.updatedAt = LocalDateTime.now();
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
        if (nombre.length() > 100) {
            throw new IllegalArgumentException("El nombre de la categoría no puede superar los 100 caracteres");
        }
        return nombre.trim();
    }
}
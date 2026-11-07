package com.tienda.backend.permiso.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/** Permiso del sistema. Catálogo fijo: se define por migración y la API solo lo consulta. */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permiso {
    @EqualsAndHashCode.Include
    private final Long id;

    private final String nombre;
    private final String descripcion;

    public Permiso(Long id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}

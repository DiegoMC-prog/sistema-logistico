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
    /** Módulo al que pertenece (fila de la matriz de permisos). */
    private final String modulo;
    /** Acción dentro del módulo (columna de la matriz). */
    private final String accion;
    /** true si la acción es VER, CREAR, EDITAR o ELIMINAR; false si es una acción especial del módulo. */
    private final boolean estandar;
    private final String descripcion;

    public Permiso(Long id, String nombre, String modulo, String accion, boolean estandar, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.modulo = modulo;
        this.accion = accion;
        this.estandar = estandar;
        this.descripcion = descripcion;
    }
}

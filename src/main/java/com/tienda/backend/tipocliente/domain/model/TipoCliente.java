package com.tienda.backend.tipocliente.domain.model;

import com.tienda.backend.tipocliente.domain.exception.TipoClienteDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TipoCliente {
    /** Tipo de sistema: es el que reciben por defecto los clientes y las ventas sin tipo. No se renombra, desactiva ni elimina. */
    public static final String NOMBRE_GENERAL = "GENERAL";
    private static final int NOMBRE_MAX = 100;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private EstadoTipoCliente estado;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public TipoCliente(Long id, String nombre, EstadoTipoCliente estado) {
        this.id = id;
        setNombre(nombre);
        setEstado(estado);
    }

    /** Tipo nuevo: nace ACTIVO y sin id. */
    public TipoCliente(String nombre) {
        this(null, nombre, EstadoTipoCliente.ACTIVO);
    }

    public void editarNombre(String nuevoNombre) {
        setNombre(nuevoNombre);
    }

    public void activar() {
        this.estado = EstadoTipoCliente.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoTipoCliente.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoTipoCliente.ACTIVO;
    }

    public boolean esDeSistema() {
        return NOMBRE_GENERAL.equals(nombre);
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new TipoClienteDatosInvalidosException("El nombre del tipo de cliente es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim().toUpperCase(Locale.ROOT);
        if (limpio.length() > NOMBRE_MAX) {
            throw new TipoClienteDatosInvalidosException(
                    "El nombre del tipo de cliente no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setEstado(EstadoTipoCliente estado) {
        if (estado == null) {
            throw new TipoClienteDatosInvalidosException("El estado del tipo de cliente es obligatorio");
        }
        this.estado = estado;
    }
}

package com.tienda.backend.deposito.domain.model;

import com.tienda.backend.deposito.domain.exception.DepositoDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Deposito {
    private static final int NOMBRE_MAX = 150;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private String descripcion;
    private EstadoDeposito estado;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public Deposito(Long id, String nombre, String descripcion, EstadoDeposito estado) {
        this.id = id;
        setNombre(nombre);
        setEstado(estado);
        this.descripcion = normalizarDescripcion(descripcion);
    }

    /** Depósito nuevo: nace ACTIVO y sin id. */
    public Deposito(String nombre, String descripcion) {
        this(null, nombre, descripcion, EstadoDeposito.ACTIVO);
    }

    public void editar(String nombre, String descripcion) {
        setNombre(nombre);
        this.descripcion = normalizarDescripcion(descripcion);
    }

    public void activar() {
        this.estado = EstadoDeposito.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoDeposito.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoDeposito.ACTIVO;
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DepositoDatosInvalidosException("El nombre del depósito es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > NOMBRE_MAX) {
            throw new DepositoDatosInvalidosException(
                    "El nombre del depósito no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setEstado(EstadoDeposito estado) {
        if (estado == null) {
            throw new DepositoDatosInvalidosException("El estado del depósito es obligatorio");
        }
        this.estado = estado;
    }

    private String normalizarDescripcion(String descripcion) {
        return (descripcion == null || descripcion.isBlank()) ? null : descripcion.trim();
    }
}

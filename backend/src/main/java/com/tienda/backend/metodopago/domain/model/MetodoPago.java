package com.tienda.backend.metodopago.domain.model;

import com.tienda.backend.metodopago.domain.exception.MetodoPagoDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MetodoPago {
    /** Método de sistema: caja lo usa para arqueos. No se renombra, desactiva ni elimina. */
    public static final String NOMBRE_EFECTIVO = "EFECTIVO";
    /** Método interno: el crédito de una devolución aplicado a la venta de un cambio. No sirve para cobrar ni reembolsar. */
    public static final String NOMBRE_CREDITO = "CREDITO_DEVOLUCION";
    private static final int NOMBRE_MAX = 100;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private EstadoMetodoPago estado;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public MetodoPago(Long id, String nombre, EstadoMetodoPago estado) {
        this.id = id;
        setNombre(nombre);
        setEstado(estado);
    }

    /** Método nuevo: nace ACTIVO y sin id. */
    public MetodoPago(String nombre) {
        this(null, nombre, EstadoMetodoPago.ACTIVO);
    }

    public void editarNombre(String nuevoNombre) {
        setNombre(nuevoNombre);
    }

    public void activar() {
        this.estado = EstadoMetodoPago.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoMetodoPago.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoMetodoPago.ACTIVO;
    }

    public boolean esDeSistema() {
        return NOMBRE_EFECTIVO.equals(nombre) || NOMBRE_CREDITO.equals(nombre);
    }

    /** false para el crédito de devolución: el frontend no debe ofrecerlo como método de cobro. */
    public boolean sirveParaCobrar() {
        return !NOMBRE_CREDITO.equals(nombre);
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new MetodoPagoDatosInvalidosException("El nombre del método de pago es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim().toUpperCase(Locale.ROOT);
        if (limpio.length() > NOMBRE_MAX) {
            throw new MetodoPagoDatosInvalidosException(
                    "El nombre del método de pago no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setEstado(EstadoMetodoPago estado) {
        if (estado == null) {
            throw new MetodoPagoDatosInvalidosException("El estado del método de pago es obligatorio");
        }
        this.estado = estado;
    }
}

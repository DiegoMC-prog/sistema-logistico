package com.tienda.backend.caja.domain.model;

import com.tienda.backend.caja.domain.exception.CajaDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Caja {
    private static final int NOMBRE_MAX = 100;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private Long depositoId;
    private EstadoCaja estado;

    /** Reconstrucción desde persistencia. */
    public Caja(Long id, String nombre, Long depositoId, EstadoCaja estado) {
        this.id = id;
        setNombre(nombre);
        setDepositoId(depositoId);
        if (estado == null) {
            throw new CajaDatosInvalidosException("El estado de la caja es obligatorio");
        }
        this.estado = estado;
    }

    /** Caja nueva: nace ACTIVA y sin id. */
    public Caja(String nombre, Long depositoId) {
        this(null, nombre, depositoId, EstadoCaja.ACTIVO);
    }

    public void editar(String nombre, Long depositoId) {
        setNombre(nombre);
        setDepositoId(depositoId);
    }

    public void activar() {
        this.estado = EstadoCaja.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoCaja.INACTIVO;
    }

    public boolean estaActiva() {
        return estado == EstadoCaja.ACTIVO;
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new CajaDatosInvalidosException("El nombre de la caja es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > NOMBRE_MAX) {
            throw new CajaDatosInvalidosException("El nombre de la caja no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setDepositoId(Long depositoId) {
        if (depositoId == null) {
            throw new CajaDatosInvalidosException("El depósito de la caja es obligatorio");
        }
        this.depositoId = depositoId;
    }
}

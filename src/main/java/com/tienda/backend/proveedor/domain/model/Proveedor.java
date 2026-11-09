package com.tienda.backend.proveedor.domain.model;

import com.tienda.backend.proveedor.domain.exception.ProveedorDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proveedor {
    private static final int EMPRESA_MAX = 100;
    private static final int CONTACTO_MAX = 150;
    private static final int TELEFONO_MAX = 30;
    private static final int EMAIL_MAX = 100;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombreEmpresa;
    private String nombreContacto;
    private String telefono;
    private String email;
    private EstadoProveedor estado;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public Proveedor(Long id, String nombreEmpresa, String nombreContacto, String telefono, String email,
                     EstadoProveedor estado) {
        this.id = id;
        setNombreEmpresa(nombreEmpresa);
        setNombreContacto(nombreContacto);
        setTelefono(telefono);
        setEmail(email);
        setEstado(estado);
    }

    /** Proveedor nuevo: nace ACTIVO y sin id. */
    public Proveedor(String nombreEmpresa, String nombreContacto, String telefono, String email) {
        this(null, nombreEmpresa, nombreContacto, telefono, email, EstadoProveedor.ACTIVO);
    }

    public void editar(String nombreEmpresa, String nombreContacto, String telefono, String email) {
        setNombreEmpresa(nombreEmpresa);
        setNombreContacto(nombreContacto);
        setTelefono(telefono);
        setEmail(email);
    }

    public void activar() {
        this.estado = EstadoProveedor.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoProveedor.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoProveedor.ACTIVO;
    }

    private void setNombreEmpresa(String valor) {
        this.nombreEmpresa = obligatorio(valor, "El nombre de la empresa", EMPRESA_MAX);
    }

    private void setNombreContacto(String valor) {
        this.nombreContacto = obligatorio(valor, "El nombre del contacto", CONTACTO_MAX);
    }

    private void setTelefono(String valor) {
        this.telefono = opcional(valor, "El teléfono", TELEFONO_MAX);
    }

    private void setEmail(String valor) {
        this.email = opcional(valor, "El email", EMAIL_MAX);
    }

    private void setEstado(EstadoProveedor estado) {
        if (estado == null) {
            throw new ProveedorDatosInvalidosException("El estado del proveedor es obligatorio");
        }
        this.estado = estado;
    }

    private static String obligatorio(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw new ProveedorDatosInvalidosException(campo + " es obligatorio y no puede estar vacío");
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw new ProveedorDatosInvalidosException(campo + " no puede superar los " + max + " caracteres");
        }
        return limpio;
    }

    private static String opcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw new ProveedorDatosInvalidosException(campo + " no puede superar los " + max + " caracteres");
        }
        return limpio;
    }
}

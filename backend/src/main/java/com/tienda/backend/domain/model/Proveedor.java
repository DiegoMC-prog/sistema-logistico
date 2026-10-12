package com.tienda.backend.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proveedor {

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombreEmpresa;
    private String direccionEmpresa;
    private String telefonoEmpresa;
    private String nombreContacto;
    private String telefonoContacto;
    private Boolean estado;

    public Proveedor(Long id, String nombreEmpresa, String direccionEmpresa, String telefonoEmpresa,
                     String nombreContacto, String telefonoContacto, Boolean estado) {
        validarNombreEmpresa(nombreEmpresa);
        validarDireccionEmpresa(direccionEmpresa);
        validarTelefono(telefonoEmpresa, "de la empresa");
        validarNombreContacto(nombreContacto);
        validarTelefono(telefonoContacto, "de contacto");

        this.id = id;
        this.nombreEmpresa = nombreEmpresa.trim();
        this.direccionEmpresa = direccionEmpresa != null ? direccionEmpresa.trim() : null;
        this.telefonoEmpresa = telefonoEmpresa != null ? telefonoEmpresa.trim() : null;
        this.nombreContacto = nombreContacto != null ? nombreContacto.trim() : null;
        this.telefonoContacto = telefonoContacto != null ? telefonoContacto.trim() : null;
        this.estado = estado != null ? estado : Boolean.TRUE;
    }

    public static Proveedor crear(String nombreEmpresa, String direccionEmpresa, String telefonoEmpresa,
                                  String nombreContacto, String telefonoContacto) {
        return new Proveedor(null, nombreEmpresa, direccionEmpresa, telefonoEmpresa, nombreContacto, telefonoContacto, true);
    }

    public void actualizar(String nuevoNombreEmpresa, String nuevaDireccionEmpresa, String nuevoTelefonoEmpresa,
                           String nuevoNombreContacto, String nuevoTelefonoContacto) {
        validarNombreEmpresa(nuevoNombreEmpresa);
        validarDireccionEmpresa(nuevaDireccionEmpresa);
        validarTelefono(nuevoTelefonoEmpresa, "de la empresa");
        validarNombreContacto(nuevoNombreContacto);
        validarTelefono(nuevoTelefonoContacto, "de contacto");

        this.nombreEmpresa = nuevoNombreEmpresa.trim();
        this.direccionEmpresa = nuevaDireccionEmpresa != null ? nuevaDireccionEmpresa.trim() : null;
        this.telefonoEmpresa = nuevoTelefonoEmpresa != null ? nuevoTelefonoEmpresa.trim() : null;
        this.nombreContacto = nuevoNombreContacto != null ? nuevoNombreContacto.trim() : null;
        this.telefonoContacto = nuevoTelefonoContacto != null ? nuevoTelefonoContacto.trim() : null;
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

    private void validarNombreEmpresa(String nombreEmpresa) {
        if (nombreEmpresa == null || nombreEmpresa.isBlank()) {
            throw new IllegalArgumentException("El nombre de la empresa proveedora es obligatorio y no puede estar vacío.");
        }
        if (nombreEmpresa.length() > 100) {
            throw new IllegalArgumentException("El nombre de la empresa proveedora no puede superar los 100 caracteres.");
        }
    }

    private void validarDireccionEmpresa(String direccionEmpresa) {
        if (direccionEmpresa != null && direccionEmpresa.length() > 250) {
            throw new IllegalArgumentException("La dirección de la empresa no puede superar los 250 caracteres.");
        }
    }

    private void validarNombreContacto(String nombreContacto) {
        if (nombreContacto != null && nombreContacto.length() > 100) {
            throw new IllegalArgumentException("El nombre de contacto no puede superar los 100 caracteres.");
        }
    }

    private void validarTelefono(String telefono, String tipo) {
        if (telefono != null && telefono.length() > 20) {
            throw new IllegalArgumentException("El teléfono " + tipo + " no puede superar los 20 caracteres.");
        }
    }
}

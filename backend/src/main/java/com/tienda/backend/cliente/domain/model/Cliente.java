package com.tienda.backend.cliente.domain.model;

import com.tienda.backend.cliente.domain.exception.ClienteDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cliente {
    private static final int NOMBRE_MAX = 150;
    private static final int DOCUMENTO_MAX = 30;
    private static final int TELEFONO_MAX = 30;
    private static final int EMAIL_MAX = 255;
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @EqualsAndHashCode.Include
    private final Long id;

    /** Puede ser null solo mientras se crea: el servicio asigna el tipo GENERAL por defecto. */
    private Long tipoClienteId;
    private String documento;
    private String nombre;
    private String telefono;
    private String email;
    private EstadoCliente estado;
    /** RF-28: si puede comprar a crédito (fiado) y hasta cuánto puede deber en total. */
    private boolean creditoHabilitado;
    private BigDecimal limiteCredito;

    /** Reconstrucción desde persistencia. */
    public Cliente(Long id, Long tipoClienteId, String documento, String nombre, String telefono, String email,
                   EstadoCliente estado, boolean creditoHabilitado, BigDecimal limiteCredito) {
        this.id = id;
        this.tipoClienteId = tipoClienteId;
        setDocumento(documento);
        setNombre(nombre);
        setTelefono(telefono);
        setEmail(email);
        if (estado == null) {
            throw new ClienteDatosInvalidosException("El estado del cliente es obligatorio");
        }
        this.estado = estado;
        setCredito(creditoHabilitado, limiteCredito);
    }

    /** Cliente nuevo: nace ACTIVO y sin id. */
    public Cliente(Long tipoClienteId, String documento, String nombre, String telefono, String email,
                   boolean creditoHabilitado, BigDecimal limiteCredito) {
        this(null, tipoClienteId, documento, nombre, telefono, email, EstadoCliente.ACTIVO, creditoHabilitado, limiteCredito);
    }

    public void editar(Long tipoClienteId, String documento, String nombre, String telefono, String email,
                       boolean creditoHabilitado, BigDecimal limiteCredito) {
        setCredito(creditoHabilitado, limiteCredito);
        this.tipoClienteId = tipoClienteId;
        setDocumento(documento);
        setNombre(nombre);
        setTelefono(telefono);
        setEmail(email);
    }

    public void asignarTipo(Long tipoClienteId) {
        this.tipoClienteId = tipoClienteId;
    }

    public void activar() {
        this.estado = EstadoCliente.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoCliente.INACTIVO;
    }

    private void setCredito(boolean habilitado, BigDecimal limite) {
        BigDecimal valor = limite == null ? BigDecimal.ZERO : limite;
        if (valor.signum() < 0 || valor.stripTrailingZeros().scale() > 2 || valor.compareTo(new BigDecimal("999999999999")) > 0) {
            throw new ClienteDatosInvalidosException("El límite de crédito debe ser mayor o igual a cero, con hasta 2 decimales");
        }
        if (habilitado && valor.signum() == 0) {
            throw new ClienteDatosInvalidosException("Para habilitar el crédito indique un límite mayor a cero");
        }
        this.creditoHabilitado = habilitado;
        this.limiteCredito = valor.setScale(2, RoundingMode.HALF_UP);
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ClienteDatosInvalidosException("El nombre del cliente es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > NOMBRE_MAX) {
            throw new ClienteDatosInvalidosException("El nombre del cliente no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setDocumento(String documento) {
        String limpio = vacioANull(documento);
        if (limpio != null && limpio.length() > DOCUMENTO_MAX) {
            throw new ClienteDatosInvalidosException("El documento no puede superar los " + DOCUMENTO_MAX + " caracteres");
        }
        this.documento = limpio;
    }

    private void setTelefono(String telefono) {
        String limpio = vacioANull(telefono);
        if (limpio != null && limpio.length() > TELEFONO_MAX) {
            throw new ClienteDatosInvalidosException("El teléfono no puede superar los " + TELEFONO_MAX + " caracteres");
        }
        this.telefono = limpio;
    }

    private void setEmail(String email) {
        String limpio = vacioANull(email);
        if (limpio != null) {
            limpio = limpio.toLowerCase(Locale.ROOT);
            if (limpio.length() > EMAIL_MAX) {
                throw new ClienteDatosInvalidosException("El email no puede superar los " + EMAIL_MAX + " caracteres");
            }
            if (!EMAIL.matcher(limpio).matches()) {
                throw new ClienteDatosInvalidosException("El email del cliente no tiene un formato válido");
            }
        }
        this.email = limpio;
    }

    private static String vacioANull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}

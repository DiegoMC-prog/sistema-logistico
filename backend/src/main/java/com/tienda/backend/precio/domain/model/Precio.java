package com.tienda.backend.precio.domain.model;

import com.tienda.backend.precio.domain.exception.PrecioDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Precio de una presentación: general (sin tipo de cliente) o por tipo de cliente, desde una cantidad mínima. */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Precio {
    private static final int ESCALA = 4;
    private static final BigDecimal MAXIMO = new BigDecimal("9999999999.9999");

    @EqualsAndHashCode.Include
    private final Long id;

    private final Long presentacionId;
    /** null = precio general. */
    private final Long tipoClienteId;

    private BigDecimal cantidadMinima;
    private BigDecimal precio;
    private EstadoPrecio estado;

    /** Reconstrucción desde persistencia. */
    public Precio(Long id, Long presentacionId, Long tipoClienteId, BigDecimal cantidadMinima, BigDecimal precio,
                  EstadoPrecio estado) {
        if (presentacionId == null) {
            throw new PrecioDatosInvalidosException("La presentación del precio es obligatoria");
        }
        if (estado == null) {
            throw new PrecioDatosInvalidosException("El estado del precio es obligatorio");
        }
        this.id = id;
        this.presentacionId = presentacionId;
        this.tipoClienteId = tipoClienteId;
        this.estado = estado;
        setCantidadMinima(cantidadMinima);
        setPrecio(precio);
    }

    /** Precio nuevo: nace ACTIVO; sin cantidad mínima se asume 1. */
    public Precio(Long presentacionId, Long tipoClienteId, BigDecimal cantidadMinima, BigDecimal precio) {
        this(null, presentacionId, tipoClienteId, cantidadMinima, precio, EstadoPrecio.ACTIVO);
    }

    public void editar(BigDecimal cantidadMinima, BigDecimal precio) {
        setCantidadMinima(cantidadMinima);
        setPrecio(precio);
    }

    public void activar() {
        this.estado = EstadoPrecio.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoPrecio.INACTIVO;
    }

    public boolean esGeneral() {
        return tipoClienteId == null;
    }

    private void setCantidadMinima(BigDecimal valor) {
        BigDecimal v = valor == null ? BigDecimal.ONE : valor;
        if (v.signum() <= 0) {
            throw new PrecioDatosInvalidosException("La cantidad mínima debe ser mayor a cero");
        }
        this.cantidadMinima = normalizar(v, "La cantidad mínima");
    }

    private void setPrecio(BigDecimal valor) {
        if (valor == null) {
            throw new PrecioDatosInvalidosException("El precio es obligatorio");
        }
        if (valor.signum() < 0) {
            throw new PrecioDatosInvalidosException("El precio no puede ser negativo");
        }
        this.precio = normalizar(valor, "El precio");
    }

    private static BigDecimal normalizar(BigDecimal v, String campo) {
        BigDecimal n = v.setScale(ESCALA, RoundingMode.HALF_UP);
        if (n.compareTo(MAXIMO) > 0) {
            throw new PrecioDatosInvalidosException(campo + " supera el valor máximo permitido");
        }
        return n;
    }
}

package com.tienda.backend.lote.domain.model;

import com.tienda.backend.lote.domain.exception.LoteDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Lote {
    private static final int NUMERO_LOTE_MAX = 80;
    private static final BigDecimal PORCENTAJE_MAX = BigDecimal.valueOf(100);

    @EqualsAndHashCode.Include
    private final Long id;

    private final Long productoId;

    private String numeroLote;
    private LocalDate fechaVencimiento;
    private BigDecimal costoUnitarioCompra;
    private BigDecimal porcentajeDescuentoVencimiento;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public Lote(Long id, Long productoId, String numeroLote, LocalDate fechaVencimiento,
                BigDecimal costoUnitarioCompra, BigDecimal porcentajeDescuentoVencimiento) {
        if (productoId == null) {
            throw new LoteDatosInvalidosException("El producto del lote es obligatorio");
        }
        this.id = id;
        this.productoId = productoId;
        aplicarDatos(numeroLote, fechaVencimiento, costoUnitarioCompra, porcentajeDescuentoVencimiento);
    }

    /** Lote nuevo, sin id. */
    public Lote(Long productoId, String numeroLote, LocalDate fechaVencimiento,
                BigDecimal costoUnitarioCompra, BigDecimal porcentajeDescuentoVencimiento) {
        this(null, productoId, numeroLote, fechaVencimiento, costoUnitarioCompra, porcentajeDescuentoVencimiento);
    }

    public void editar(String numeroLote, LocalDate fechaVencimiento,
                       BigDecimal costoUnitarioCompra, BigDecimal porcentajeDescuentoVencimiento) {
        aplicarDatos(numeroLote, fechaVencimiento, costoUnitarioCompra, porcentajeDescuentoVencimiento);
    }

    private void aplicarDatos(String numeroLote, LocalDate fechaVencimiento,
                              BigDecimal costoUnitarioCompra, BigDecimal porcentajeDescuentoVencimiento) {
        String numeroLimpio = validarNumeroLote(numeroLote);
        BigDecimal costo = validarCosto(costoUnitarioCompra);
        BigDecimal descuento = validarDescuento(porcentajeDescuentoVencimiento);

        this.numeroLote = numeroLimpio;
        this.fechaVencimiento = fechaVencimiento;
        this.costoUnitarioCompra = costo;
        this.porcentajeDescuentoVencimiento = descuento;
    }

    private String validarNumeroLote(String numeroLote) {
        if (numeroLote == null || numeroLote.isBlank()) {
            throw new LoteDatosInvalidosException("El número de lote es obligatorio y no puede estar vacío");
        }
        String limpio = numeroLote.trim();
        if (limpio.length() > NUMERO_LOTE_MAX) {
            throw new LoteDatosInvalidosException(
                    "El número de lote no puede superar los " + NUMERO_LOTE_MAX + " caracteres");
        }
        return limpio;
    }

    private BigDecimal validarCosto(BigDecimal costo) {
        if (costo == null) {
            throw new LoteDatosInvalidosException("El costo unitario de compra es obligatorio");
        }
        if (costo.signum() < 0) {
            throw new LoteDatosInvalidosException("El costo unitario de compra no puede ser negativo");
        }
        return costo;
    }

    private BigDecimal validarDescuento(BigDecimal descuento) {
        if (descuento == null) {
            return BigDecimal.ZERO;
        }
        if (descuento.signum() < 0 || descuento.compareTo(PORCENTAJE_MAX) > 0) {
            throw new LoteDatosInvalidosException("El porcentaje de descuento por vencimiento debe estar entre 0 y 100");
        }
        return descuento;
    }
}

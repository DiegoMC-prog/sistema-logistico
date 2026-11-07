package com.tienda.backend.presentacion.domain.model;

import com.tienda.backend.presentacion.domain.exception.PresentacionDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Presentacion {
    private static final int CODIGO_BARRAS_MAX = 64;

    @EqualsAndHashCode.Include
    private final Long id;

    private final Long productoId;
    private final boolean esUnidadBase;

    private Long unidadMedidaId;
    private String codigoBarras;
    private BigDecimal factorConversion;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public Presentacion(Long id, Long productoId, Long unidadMedidaId, String codigoBarras,
                        BigDecimal factorConversion, boolean esUnidadBase) {
        if (productoId == null) {
            throw new PresentacionDatosInvalidosException("El producto de la presentación es obligatorio");
        }
        this.id = id;
        this.productoId = productoId;
        this.esUnidadBase = esUnidadBase;
        setUnidadMedidaId(unidadMedidaId);
        setCodigoBarras(codigoBarras);
        setFactorConversion(factorConversion);
    }

    /** Presentación nueva, sin id. */
    public Presentacion(Long productoId, Long unidadMedidaId, String codigoBarras,
                        BigDecimal factorConversion, boolean esUnidadBase) {
        this(null, productoId, unidadMedidaId, codigoBarras, factorConversion, esUnidadBase);
    }

    /**
     * La unidad base es inmutable (el stock ya guardado está expresado en ella) y su factor es siempre 1;
     * en ella solo se puede cambiar el código de barras.
     */
    public void editar(Long unidadMedidaId, String codigoBarras, BigDecimal factorConversion) {
        if (esUnidadBase && !this.unidadMedidaId.equals(unidadMedidaId)) {
            throw new PresentacionDatosInvalidosException("La unidad de la presentación base no se puede cambiar");
        }
        setUnidadMedidaId(unidadMedidaId);
        setCodigoBarras(codigoBarras);
        setFactorConversion(factorConversion);
    }

    private void setUnidadMedidaId(Long unidadMedidaId) {
        if (unidadMedidaId == null) {
            throw new PresentacionDatosInvalidosException("La unidad de medida es obligatoria");
        }
        this.unidadMedidaId = unidadMedidaId;
    }

    private void setCodigoBarras(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.isBlank()) {
            throw new PresentacionDatosInvalidosException("El código de barras es obligatorio y no puede estar vacío");
        }
        String limpio = codigoBarras.trim();
        if (limpio.length() > CODIGO_BARRAS_MAX) {
            throw new PresentacionDatosInvalidosException(
                    "El código de barras no puede superar los " + CODIGO_BARRAS_MAX + " caracteres");
        }
        this.codigoBarras = limpio;
    }

    private void setFactorConversion(BigDecimal factorConversion) {
        if (factorConversion == null) {
            throw new PresentacionDatosInvalidosException("El factor de conversión es obligatorio");
        }
        if (factorConversion.signum() <= 0) {
            throw new PresentacionDatosInvalidosException("El factor de conversión debe ser mayor que cero");
        }
        if (esUnidadBase && factorConversion.compareTo(BigDecimal.ONE) != 0) {
            throw new PresentacionDatosInvalidosException("El factor de conversión de la unidad base debe ser 1");
        }
        this.factorConversion = factorConversion;
    }
}

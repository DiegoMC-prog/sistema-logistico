package com.tienda.backend.inventario.domain.model;

import com.tienda.backend.inventario.domain.exception.MovimientoInvalidoException;
import com.tienda.backend.inventario.domain.exception.StockInsuficienteException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Stock de un lote en un depósito, en unidad base del producto. Nunca queda negativo: {@link #disminuir} lo impide.
 * {@code version} es el número de versión para el bloqueo optimista: detecta dos cambios simultáneos sobre el mismo stock.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Inventario {
    @EqualsAndHashCode.Include
    private final Long id;

    private final Long depositoId;
    private final Long loteId;
    private final Long version;
    private BigDecimal stockActual;

    public Inventario(Long id, Long depositoId, Long loteId, BigDecimal stockActual, Long version) {
        this.id = id;
        this.depositoId = depositoId;
        this.loteId = loteId;
        this.stockActual = stockActual;
        this.version = version;
    }

    /** Fila nueva con stock cero, para el primer ingreso de un lote en un depósito. */
    public static Inventario vacio(Long depositoId, Long loteId) {
        return new Inventario(null, depositoId, loteId, BigDecimal.ZERO, null);
    }

    public void aumentar(BigDecimal cantidad) {
        validar(cantidad);
        this.stockActual = stockActual.add(cantidad);
    }

    public void disminuir(BigDecimal cantidad) {
        validar(cantidad);
        if (stockActual.compareTo(cantidad) < 0) {
            throw new StockInsuficienteException(loteId, stockActual, cantidad);
        }
        this.stockActual = stockActual.subtract(cantidad);
    }

    private void validar(BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new MovimientoInvalidoException("La cantidad debe ser mayor que cero");
        }
    }
}

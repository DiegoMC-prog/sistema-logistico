package com.tienda.backend.pedido.domain.model;

import com.tienda.backend.pedido.domain.exception.PedidoDatosInvalidosException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Línea de un pedido: producto, cantidad pedida (en unidad base) y costo unitario pactado. */
@Getter
public class LineaPedido {
    private static final int ESCALA = 4;

    private final Long id;
    private final Long productoId;
    private final BigDecimal cantidadPedida;
    private BigDecimal cantidadRecibida;
    private final BigDecimal costoUnitario;

    /** Reconstrucción desde persistencia. */
    public LineaPedido(Long id, Long productoId, BigDecimal cantidadPedida, BigDecimal cantidadRecibida,
                       BigDecimal costoUnitario) {
        if (productoId == null) {
            throw new PedidoDatosInvalidosException("Cada línea del pedido debe indicar el producto");
        }
        if (cantidadPedida == null || cantidadPedida.signum() <= 0) {
            throw new PedidoDatosInvalidosException("La cantidad pedida debe ser mayor que cero");
        }
        if (cantidadPedida.scale() > ESCALA) {
            throw new PedidoDatosInvalidosException("La cantidad admite como máximo " + ESCALA + " decimales");
        }
        if (costoUnitario == null || costoUnitario.signum() < 0) {
            throw new PedidoDatosInvalidosException("El costo unitario no puede ser negativo");
        }
        if (costoUnitario.scale() > ESCALA) {
            throw new PedidoDatosInvalidosException("El costo unitario admite como máximo " + ESCALA + " decimales");
        }
        this.id = id;
        this.productoId = productoId;
        this.cantidadPedida = cantidadPedida;
        this.cantidadRecibida = cantidadRecibida == null ? BigDecimal.ZERO : cantidadRecibida;
        this.costoUnitario = costoUnitario;
    }

    /** Línea nueva: sin id y sin nada recibido. */
    public LineaPedido(Long productoId, BigDecimal cantidadPedida, BigDecimal costoUnitario) {
        this(null, productoId, cantidadPedida, BigDecimal.ZERO, costoUnitario);
    }

    public BigDecimal pendiente() {
        return cantidadPedida.subtract(cantidadRecibida);
    }

    public BigDecimal subtotal() {
        return cantidadPedida.multiply(costoUnitario).setScale(ESCALA, RoundingMode.HALF_UP);
    }

    public boolean completa() {
        return pendiente().signum() == 0;
    }

    void recibir(BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new PedidoDatosInvalidosException("La cantidad recibida debe ser mayor que cero");
        }
        if (cantidad.scale() > ESCALA) {
            throw new PedidoDatosInvalidosException("La cantidad admite como máximo " + ESCALA + " decimales");
        }
        if (cantidad.compareTo(pendiente()) > 0) {
            throw new PedidoDatosInvalidosException("No se puede recibir " + cantidad.stripTrailingZeros().toPlainString()
                    + ": solo quedan " + pendiente().stripTrailingZeros().toPlainString()
                    + " pendientes de la línea del producto " + productoId);
        }
        this.cantidadRecibida = cantidadRecibida.add(cantidad);
    }
}

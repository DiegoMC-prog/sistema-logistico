package com.tienda.backend.devolucion.domain.model;

import com.tienda.backend.devolucion.domain.exception.DevolucionDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Devolución de parte o todo lo vendido. Puede reembolsarse (con un método de pago) o ser un cambio: entonces el valor
 * de lo devuelto se aplica como crédito a {@code ventaCambioId}, la venta que se llevó el cliente.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Devolucion {
    private static final int MOTIVO_MAX = 255;

    @EqualsAndHashCode.Include
    private final Long id;

    private final String numeroDevolucion;
    private final Long ventaId;
    private final Long usuarioId;
    private final Long turnoId;
    private final LocalDateTime fecha;
    private final String motivo;
    private final BigDecimal montoReembolsado;
    private final Long metodoPagoId;
    private final String idempotencyKey;
    private final String idempotencyHash;
    private final List<LineaDevuelta> lineas;

    private Long ventaCambioId;

    /** Reconstrucción desde persistencia. */
    public Devolucion(Long id, String numeroDevolucion, Long ventaId, Long usuarioId, Long turnoId, LocalDateTime fecha,
                      String motivo, BigDecimal montoReembolsado, Long metodoPagoId, Long ventaCambioId,
                      String idempotencyKey, String idempotencyHash, List<LineaDevuelta> lineas) {
        if (motivo == null || motivo.isBlank()) {
            throw new DevolucionDatosInvalidosException("El motivo de la devolución es obligatorio");
        }
        if (motivo.trim().length() > MOTIVO_MAX) {
            throw new DevolucionDatosInvalidosException("El motivo no puede superar los " + MOTIVO_MAX + " caracteres");
        }
        if (montoReembolsado.signum() > 0 && metodoPagoId == null) {
            throw new DevolucionDatosInvalidosException("Un reembolso requiere indicar el método de pago");
        }
        this.id = id;
        this.numeroDevolucion = numeroDevolucion;
        this.ventaId = ventaId;
        this.usuarioId = usuarioId;
        this.turnoId = turnoId;
        this.fecha = fecha;
        this.motivo = motivo.trim();
        this.montoReembolsado = montoReembolsado;
        this.metodoPagoId = metodoPagoId;
        this.ventaCambioId = ventaCambioId;
        this.idempotencyKey = idempotencyKey;
        this.idempotencyHash = idempotencyHash;
        this.lineas = List.copyOf(lineas);
    }

    public static Devolucion registrar(String numeroDevolucion, Long ventaId, Long usuarioId, Long turnoId, String motivo,
                                       BigDecimal montoReembolsado, Long metodoPagoId, String idempotencyKey,
                                       String idempotencyHash, List<LineaDevuelta> lineas) {
        if (lineas.isEmpty()) {
            throw new DevolucionDatosInvalidosException("La devolución debe tener al menos una línea");
        }
        return new Devolucion(null, numeroDevolucion, ventaId, usuarioId, turnoId, LocalDateTime.now(), motivo,
                montoReembolsado, metodoPagoId, null, idempotencyKey, idempotencyHash, lineas);
    }

    /** Enlaza la venta que se llevó el cliente en un cambio. */
    public void vincularVentaCambio(Long ventaCambioId) {
        this.ventaCambioId = ventaCambioId;
    }

    public boolean esDe(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }
}

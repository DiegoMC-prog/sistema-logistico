package com.tienda.backend.caja.domain.model;

import com.tienda.backend.caja.domain.exception.CajaDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Entrada o salida de efectivo que no es una venta. No se edita ni se borra: un error se corrige con otro movimiento. */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MovimientoCaja {
    private static final int MOTIVO_MAX = 255;

    @EqualsAndHashCode.Include
    private final Long id;

    private final Long turnoId;
    private final Long usuarioId;
    private final TipoMovimientoCaja tipo;
    private final BigDecimal monto;
    private final String motivo;
    private final LocalDateTime fecha;

    /** Reconstrucción desde persistencia. */
    public MovimientoCaja(Long id, Long turnoId, Long usuarioId, TipoMovimientoCaja tipo, BigDecimal monto,
                          String motivo, LocalDateTime fecha) {
        if (turnoId == null || usuarioId == null || tipo == null || fecha == null) {
            throw new CajaDatosInvalidosException("El movimiento requiere turno, usuario, tipo y fecha");
        }
        if (monto == null || monto.signum() <= 0) {
            throw new CajaDatosInvalidosException("El monto del movimiento debe ser mayor a cero");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new CajaDatosInvalidosException("El motivo del movimiento es obligatorio");
        }
        String limpio = motivo.trim();
        if (limpio.length() > MOTIVO_MAX) {
            throw new CajaDatosInvalidosException("El motivo no puede superar los " + MOTIVO_MAX + " caracteres");
        }
        this.id = id;
        this.turnoId = turnoId;
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.monto = Dinero.normalizar(monto, "El monto");
        this.motivo = limpio;
        this.fecha = fecha;
    }

    public static MovimientoCaja registrar(Long turnoId, Long usuarioId, TipoMovimientoCaja tipo, BigDecimal monto,
                                           String motivo) {
        return new MovimientoCaja(null, turnoId, usuarioId, tipo, monto, motivo, LocalDateTime.now());
    }
}

package com.tienda.backend.inventario.domain.model;

import com.tienda.backend.inventario.domain.exception.MovimientoInvalidoException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Registro inmutable de un cambio de stock. La coherencia de depósitos según el tipo es la misma que
 * exige la BD (restricción {@code ck_movimientos_depositos}), validada aquí para dar un error claro antes de guardar.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Movimiento {
    private static final int REFERENCIA_TIPO_MAX = 50;

    @EqualsAndHashCode.Include
    private final Long id;

    private final TipoMovimiento tipo;
    private final Long depositoOrigenId;
    private final Long depositoDestinoId;
    private final Long usuarioId;
    private final LocalDateTime fecha;
    private final String observacion;
    private final String referenciaTipo;
    private final Long referenciaId;
    private final List<MovimientoDetalle> detalles;

    public Movimiento(Long id, TipoMovimiento tipo, Long depositoOrigenId, Long depositoDestinoId, Long usuarioId,
                      LocalDateTime fecha, String observacion, String referenciaTipo, Long referenciaId,
                      List<MovimientoDetalle> detalles) {
        if (tipo == null) {
            throw new MovimientoInvalidoException("El tipo de movimiento es obligatorio");
        }
        if (usuarioId == null) {
            throw new MovimientoInvalidoException("El movimiento debe registrar el usuario que lo realiza");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new MovimientoInvalidoException("El movimiento debe tener al menos una línea");
        }
        validarDepositos(tipo, depositoOrigenId, depositoDestinoId);

        this.id = id;
        this.tipo = tipo;
        this.depositoOrigenId = depositoOrigenId;
        this.depositoDestinoId = depositoDestinoId;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.observacion = (observacion == null || observacion.isBlank()) ? null : observacion.trim();
        this.referenciaTipo = normalizarReferencia(referenciaTipo);
        this.referenciaId = referenciaId;
        this.detalles = List.copyOf(detalles);
    }

    public static Movimiento nuevo(TipoMovimiento tipo, Long depositoOrigenId, Long depositoDestinoId, Long usuarioId,
                                   String observacion, String referenciaTipo, Long referenciaId,
                                   List<MovimientoDetalle> detalles) {
        return new Movimiento(null, tipo, depositoOrigenId, depositoDestinoId, usuarioId, LocalDateTime.now(),
                observacion, referenciaTipo, referenciaId, detalles);
    }

    private static void validarDepositos(TipoMovimiento tipo, Long origen, Long destino) {
        boolean valido = switch (tipo) {
            case ENTRADA, AJUSTE_POSITIVO -> destino != null && origen == null;
            case SALIDA, AJUSTE_NEGATIVO -> origen != null && destino == null;
            case TRASLADO -> origen != null && destino != null && !origen.equals(destino);
        };
        if (!valido) {
            throw new MovimientoInvalidoException("Los depósitos indicados no corresponden a un movimiento de tipo " + tipo);
        }
    }

    private static String normalizarReferencia(String referenciaTipo) {
        if (referenciaTipo == null || referenciaTipo.isBlank()) {
            return null;
        }
        String limpio = referenciaTipo.trim();
        if (limpio.length() > REFERENCIA_TIPO_MAX) {
            throw new MovimientoInvalidoException(
                    "El tipo de referencia no puede superar los " + REFERENCIA_TIPO_MAX + " caracteres");
        }
        return limpio;
    }
}

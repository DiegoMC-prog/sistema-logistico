package com.tienda.backend.caja.domain.model;

import com.tienda.backend.caja.domain.exception.CajaDatosInvalidosException;
import com.tienda.backend.caja.domain.exception.TurnoCerradoException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Periodo en que un cajero tiene la caja: del efectivo inicial contado al efectivo final contado. */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TurnoCaja {
    @EqualsAndHashCode.Include
    private final Long id;

    private final Long cajaId;
    private final Long usuarioId;
    private final LocalDateTime fechaApertura;
    private final BigDecimal montoApertura;

    private LocalDateTime fechaCierre;
    private BigDecimal montoCierreDeclarado;
    private BigDecimal montoCierreEsperado;
    private EstadoTurno estado;

    /** Reconstrucción desde persistencia. */
    public TurnoCaja(Long id, Long cajaId, Long usuarioId, LocalDateTime fechaApertura, BigDecimal montoApertura,
                     LocalDateTime fechaCierre, BigDecimal montoCierreDeclarado, BigDecimal montoCierreEsperado,
                     EstadoTurno estado) {
        if (cajaId == null || usuarioId == null || fechaApertura == null || estado == null) {
            throw new CajaDatosInvalidosException("El turno requiere caja, usuario, fecha de apertura y estado");
        }
        this.id = id;
        this.cajaId = cajaId;
        this.usuarioId = usuarioId;
        this.fechaApertura = fechaApertura;
        this.montoApertura = Dinero.noNegativo(montoApertura, "El monto de apertura");
        this.fechaCierre = fechaCierre;
        this.montoCierreDeclarado = montoCierreDeclarado;
        this.montoCierreEsperado = montoCierreEsperado;
        this.estado = estado;
    }

    /** Turno nuevo: nace ABIERTO ahora mismo. */
    public static TurnoCaja abrir(Long cajaId, Long usuarioId, BigDecimal montoApertura) {
        return new TurnoCaja(null, cajaId, usuarioId, LocalDateTime.now(), montoApertura, null, null, null,
                EstadoTurno.ABIERTO);
    }

    public void cerrar(BigDecimal declarado, BigDecimal esperado) {
        exigirAbierto();
        this.montoCierreDeclarado = Dinero.noNegativo(declarado, "El monto declarado");
        this.montoCierreEsperado = esperado;
        this.fechaCierre = LocalDateTime.now();
        this.estado = EstadoTurno.CERRADO;
    }

    public void exigirAbierto() {
        if (estado != EstadoTurno.ABIERTO) {
            throw new TurnoCerradoException(id);
        }
    }

    public boolean estaAbierto() {
        return estado == EstadoTurno.ABIERTO;
    }

    public boolean esDe(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }
}

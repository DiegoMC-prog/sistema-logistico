package com.tienda.backend.venta.domain.model;

import com.tienda.backend.venta.domain.exception.VentaDatosInvalidosException;
import com.tienda.backend.venta.domain.exception.VentaYaAnuladaException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Nota de venta: cabecera, líneas con los lotes usados y pagos. Se crea completa y solo puede anularse después. */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venta {
    private static final int MOTIVO_MAX = 255;
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    @EqualsAndHashCode.Include
    private final Long id;

    private final String numeroVenta;
    private final Long turnoId;
    private final Long usuarioId;
    private final Long clienteId;
    private final Long tipoClienteId;
    private final Long depositoId;
    private final LocalDateTime fechaVenta;
    private final Long version;
    private final String idempotencyKey;
    private final String idempotencyHash;

    private EstadoVenta estado;
    private BigDecimal subtotal;
    private BigDecimal descuentoTotal;
    private BigDecimal total;
    private BigDecimal vuelto;
    private String anuladaPor;
    private LocalDateTime fechaAnulacion;
    private String motivoAnulacion;
    private List<LineaVenta> lineas;
    private List<PagoVenta> pagos;

    /** Reconstrucción desde persistencia. */
    public Venta(Long id, String numeroVenta, Long turnoId, Long usuarioId, Long clienteId, Long tipoClienteId,
                 Long depositoId, EstadoVenta estado, BigDecimal subtotal, BigDecimal descuentoTotal, BigDecimal total,
                 BigDecimal vuelto, LocalDateTime fechaVenta, String anuladaPor, LocalDateTime fechaAnulacion,
                 String motivoAnulacion, Long version, String idempotencyKey, String idempotencyHash,
                 List<LineaVenta> lineas, List<PagoVenta> pagos) {
        this.id = id;
        this.numeroVenta = numeroVenta;
        this.turnoId = turnoId;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
        this.tipoClienteId = tipoClienteId;
        this.depositoId = depositoId;
        this.estado = estado;
        this.subtotal = subtotal;
        this.descuentoTotal = descuentoTotal;
        this.total = total;
        this.vuelto = vuelto;
        this.fechaVenta = fechaVenta;
        this.anuladaPor = anuladaPor;
        this.fechaAnulacion = fechaAnulacion;
        this.motivoAnulacion = motivoAnulacion;
        this.version = version;
        this.idempotencyKey = idempotencyKey;
        this.idempotencyHash = idempotencyHash;
        this.lineas = List.copyOf(lineas);
        this.pagos = List.copyOf(pagos);
    }

    /** Venta recién iniciada: sin líneas ni pagos todavía; se completa antes de confirmar la transacción. */
    public static Venta iniciar(String numeroVenta, Long turnoId, Long usuarioId, Long clienteId, Long tipoClienteId,
                                Long depositoId, String idempotencyKey, String idempotencyHash) {
        return new Venta(null, numeroVenta, turnoId, usuarioId, clienteId, tipoClienteId, depositoId,
                EstadoVenta.COMPLETADA, CERO, CERO, CERO, CERO, LocalDateTime.now(), null, null, null, null,
                idempotencyKey, idempotencyHash, List.of(), List.of());
    }

    public void completar(List<LineaVenta> lineas, List<PagoVenta> pagos, BigDecimal subtotal,
                          BigDecimal descuentoTotal, BigDecimal total, BigDecimal vuelto) {
        if (lineas.isEmpty()) {
            throw new VentaDatosInvalidosException("La venta debe tener al menos una línea");
        }
        this.lineas = List.copyOf(lineas);
        this.pagos = List.copyOf(pagos);
        this.subtotal = subtotal;
        this.descuentoTotal = descuentoTotal;
        this.total = total;
        this.vuelto = vuelto;
    }

    public void anular(String usuario, String motivo) {
        if (estado == EstadoVenta.ANULADA) {
            throw new VentaYaAnuladaException(numeroVenta);
        }
        if (motivo == null || motivo.isBlank()) {
            throw new VentaDatosInvalidosException("El motivo de la anulación es obligatorio");
        }
        String limpio = motivo.trim();
        if (limpio.length() > MOTIVO_MAX) {
            throw new VentaDatosInvalidosException("El motivo no puede superar los " + MOTIVO_MAX + " caracteres");
        }
        this.estado = EstadoVenta.ANULADA;
        this.anuladaPor = usuario;
        this.fechaAnulacion = LocalDateTime.now();
        this.motivoAnulacion = limpio;
    }

    public boolean esDe(Long usuarioId) {
        return this.usuarioId.equals(usuarioId);
    }
}

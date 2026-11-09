package com.tienda.backend.inventario.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.domain.model.Movimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MovimientoResponse(
        Long id,
        String tipo,
        LocalDateTime fecha,
        DepositoResumen depositoOrigen,
        DepositoResumen depositoDestino,
        UsuarioResumen usuario,
        String observacion,
        Referencia referencia,
        List<Linea> items
) {
    public record DepositoResumen(Long id, String nombre) {
    }

    public record UsuarioResumen(Long id, String nombre) {
    }

    public record Referencia(String tipo, Long id) {
    }

    public record ProductoResumen(Long id, String nombre) {
    }

    public record LoteResumen(Long id, String numeroLote) {
    }

    /** Cantidad en unidad base del producto. */
    public record Linea(ProductoResumen producto, LoteResumen lote, BigDecimal cantidad) {
    }

    public static MovimientoResponse from(MovimientoDetallado detallado) {
        Movimiento m = detallado.movimiento();
        return new MovimientoResponse(
                m.getId(),
                m.getTipo().name(),
                m.getFecha(),
                m.getDepositoOrigenId() == null ? null
                        : new DepositoResumen(m.getDepositoOrigenId(), detallado.depositoOrigenNombre()),
                m.getDepositoDestinoId() == null ? null
                        : new DepositoResumen(m.getDepositoDestinoId(), detallado.depositoDestinoNombre()),
                new UsuarioResumen(m.getUsuarioId(), detallado.usuarioNombre()),
                m.getObservacion(),
                m.getReferenciaTipo() == null ? null : new Referencia(m.getReferenciaTipo(), m.getReferenciaId()),
                detallado.lineas().stream()
                        .map(l -> new Linea(
                                new ProductoResumen(l.productoId(), l.productoNombre()),
                                new LoteResumen(l.loteId(), l.numeroLote()),
                                l.cantidad()))
                        .toList());
    }
}

package com.tienda.backend.devolucion.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.devolucion.application.model.DevolucionVista;
import com.tienda.backend.devolucion.application.model.VentaCambioRef;
import com.tienda.backend.devolucion.domain.model.Devolucion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DevolucionResponse(
        Long id,
        String numeroDevolucion,
        Long ventaId,
        Long usuarioId,
        Long turnoId,
        LocalDateTime fecha,
        String motivo,
        /** Lo reembolsado; en un cambio, el crédito que se aplicó a la venta nueva. */
        BigDecimal montoReembolsado,
        MetodoPagoResumen metodoPago,
        /** Solo en un cambio: la venta que se llevó el cliente. */
        VentaCambioResumen ventaCambio,
        List<LineaResponse> lineas
) {
    public record MetodoPagoResumen(Long id, String nombre) {
    }

    public record VentaCambioResumen(Long id, String numeroVenta, BigDecimal total) {
    }

    public record LineaResponse(Long ventaDetalleId, Long loteId, BigDecimal cantidadBase, boolean reingresaStock) {
    }

    public static DevolucionResponse from(DevolucionVista vista) {
        Devolucion d = vista.devolucion();
        VentaCambioRef cambio = vista.ventaCambio();
        return new DevolucionResponse(d.getId(), d.getNumeroDevolucion(), d.getVentaId(), d.getUsuarioId(), d.getTurnoId(),
                d.getFecha(), d.getMotivo(), d.getMontoReembolsado(),
                d.getMetodoPagoId() == null ? null : new MetodoPagoResumen(d.getMetodoPagoId(), vista.metodoPagoNombre()),
                cambio == null ? null : new VentaCambioResumen(cambio.id(), cambio.numeroVenta(), cambio.total()),
                d.getLineas().stream().map(l -> new LineaResponse(l.ventaDetalleId(), l.loteId(), l.cantidadBase(),
                        l.reingresaStock())).toList());
    }

    /** Fila de listado: sin líneas ni nombres. */
    public static DevolucionResponse resumen(Devolucion d) {
        return from(new DevolucionVista(d, null, null));
    }
}

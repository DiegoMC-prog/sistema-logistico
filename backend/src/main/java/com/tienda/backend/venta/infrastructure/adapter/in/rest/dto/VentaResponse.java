package com.tienda.backend.venta.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.venta.application.model.VentaVista;
import com.tienda.backend.venta.domain.model.LineaVenta;
import com.tienda.backend.venta.domain.model.PagoVenta;
import com.tienda.backend.venta.domain.model.Venta;
import com.tienda.backend.metodopago.domain.model.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Nota de venta. No expone costos de los lotes. */
public record VentaResponse(
        Long id,
        String numeroVenta,
        String estado,
        LocalDateTime fechaVenta,
        Long turnoId,
        Long usuarioId,
        Long depositoId,
        ClienteResumen cliente,
        BigDecimal subtotal,
        BigDecimal descuentoTotal,
        BigDecimal total,
        /** Efectivo entregado por el cliente (efectivo neto + vuelto); null si no pagó en efectivo. */
        BigDecimal efectivoRecibido,
        BigDecimal vuelto,
        List<LineaResponse> lineas,
        List<PagoResponse> pagos,
        AnulacionResponse anulacion
) {
    public record ClienteResumen(Long id, String nombre) {
    }

    public record LoteResponse(Long loteId, BigDecimal cantidadBase) {
    }

    public record LineaResponse(Long id, Long productoId, String producto, Long presentacionId, BigDecimal cantidad,
                                BigDecimal factorConversion, BigDecimal precioUnitario, BigDecimal descuento,
                                BigDecimal subtotal, boolean precioModificado, String motivoPrecio,
                                List<LoteResponse> lotes) {
    }

    public record PagoResponse(Long metodoPagoId, String metodoPago, BigDecimal monto, String referencia) {
    }

    public record AnulacionResponse(String anuladaPor, LocalDateTime fecha, String motivo) {
    }

    public static VentaResponse from(VentaVista vista) {
        Venta v = vista.venta();
        Map<Long, String> productos = vista.productos();
        Map<Long, String> metodos = vista.metodosPago();

        List<LineaResponse> lineas = v.getLineas().stream().map(l -> new LineaResponse(l.id(), l.productoId(),
                productos.get(l.productoId()), l.presentacionId(), l.cantidad(), l.factorConversion(),
                l.precioUnitario(), l.descuento(), l.subtotal(), l.esPrecioModificado(), l.motivoPrecio(),
                l.lotes().stream().map(x -> new LoteResponse(x.loteId(), x.cantidadBase())).toList())).toList();
        List<PagoResponse> pagos = v.getPagos().stream().map(p -> new PagoResponse(p.metodoPagoId(),
                metodos.get(p.metodoPagoId()), p.monto(), p.referencia())).toList();

        BigDecimal efectivoRecibido = v.getPagos().stream()
                .filter(p -> MetodoPago.NOMBRE_EFECTIVO.equals(metodos.get(p.metodoPagoId())))
                .map(PagoVenta::monto).findFirst().map(m -> m.add(v.getVuelto())).orElse(null);

        return new VentaResponse(v.getId(), v.getNumeroVenta(), v.getEstado().name(), v.getFechaVenta(),
                v.getTurnoId(), v.getUsuarioId(), v.getDepositoId(),
                v.getClienteId() == null ? null : new ClienteResumen(v.getClienteId(), vista.clienteNombre()),
                v.getSubtotal(), v.getDescuentoTotal(), v.getTotal(), efectivoRecibido, v.getVuelto(), lineas, pagos,
                v.getMotivoAnulacion() == null ? null
                        : new AnulacionResponse(v.getAnuladaPor(), v.getFechaAnulacion(), v.getMotivoAnulacion()));
    }
}

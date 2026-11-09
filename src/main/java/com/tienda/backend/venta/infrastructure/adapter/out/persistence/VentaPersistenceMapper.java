package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.domain.model.EstadoVenta;
import com.tienda.backend.venta.domain.model.LineaVenta;
import com.tienda.backend.venta.domain.model.LoteVendido;
import com.tienda.backend.venta.domain.model.PagoVenta;
import com.tienda.backend.venta.domain.model.Venta;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class VentaPersistenceMapper {

    /** Venta completa a partir de la cabecera y sus filas hijas ya leídas. */
    public Venta toDomain(VentaEntity e, List<VentaDetalleEntity> detalles,
                          Map<Long, List<VentaDetalleLoteEntity>> lotesPorDetalle, List<VentaPagoEntity> pagos) {
        List<LineaVenta> lineas = detalles.stream().map(d -> new LineaVenta(d.getId(), d.getProductoId(),
                d.getPresentacionId(), d.getCantidad(), d.getFactorConversion(), d.getPrecioUnitario(),
                d.getDescuento(), d.getSubtotal(), d.isEsPrecioModificado(), d.getMotivoPrecio(),
                lotesPorDetalle.getOrDefault(d.getId(), List.of()).stream()
                        .map(l -> new LoteVendido(l.getId(), l.getLoteId(), l.getCantidad(), l.getCostoUnitario()))
                        .toList())).toList();
        List<PagoVenta> pagosDominio = pagos.stream()
                .map(p -> new PagoVenta(p.getId(), p.getMetodoPagoId(), p.getMonto(), p.getReferencia())).toList();
        return new Venta(e.getId(), e.getNumeroVenta(), e.getTurnoId(), e.getUsuarioId(), e.getClienteId(),
                e.getTipoClienteId(), e.getDepositoId(), EstadoVenta.valueOf(e.getEstado()), e.getSubtotal(),
                e.getDescuentoTotal(), e.getTotal(), e.getVuelto(), e.getFechaVenta(), e.getAnuladaPor(),
                e.getFechaAnulacion(), e.getMotivoAnulacion(), e.getVersion(), e.getIdempotencyKey(),
                e.getIdempotencyHash(), lineas, pagosDominio);
    }

    /** Solo cabecera: para listados. */
    public Venta toDomainCabecera(VentaEntity e) {
        return toDomain(e, List.of(), Map.of(), List.of());
    }

    public VentaEntity toEntity(Venta v) {
        return VentaEntity.builder().id(v.getId()).numeroVenta(v.getNumeroVenta()).turnoId(v.getTurnoId())
                .usuarioId(v.getUsuarioId()).clienteId(v.getClienteId()).tipoClienteId(v.getTipoClienteId())
                .depositoId(v.getDepositoId()).estado(v.getEstado().name()).subtotal(v.getSubtotal())
                .descuentoTotal(v.getDescuentoTotal()).total(v.getTotal()).vuelto(v.getVuelto())
                .fechaVenta(v.getFechaVenta()).anuladaPor(v.getAnuladaPor()).fechaAnulacion(v.getFechaAnulacion())
                .motivoAnulacion(v.getMotivoAnulacion()).version(v.getVersion())
                .idempotencyKey(v.getIdempotencyKey()).idempotencyHash(v.getIdempotencyHash()).build();
    }
}

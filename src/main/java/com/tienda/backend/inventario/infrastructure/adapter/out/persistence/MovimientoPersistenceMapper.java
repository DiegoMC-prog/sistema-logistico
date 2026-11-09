package com.tienda.backend.inventario.infrastructure.adapter.out.persistence;

import com.tienda.backend.inventario.domain.model.Movimiento;
import com.tienda.backend.inventario.domain.model.MovimientoDetalle;
import com.tienda.backend.inventario.domain.model.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovimientoPersistenceMapper {

    public Movimiento toDomain(MovimientoEntity entity, List<MovimientoDetalleEntity> detalles) {
        return new Movimiento(
                entity.getId(),
                TipoMovimiento.valueOf(entity.getTipoMovimiento()),
                entity.getDepositoOrigenId(),
                entity.getDepositoDestinoId(),
                entity.getUsuarioId(),
                entity.getFechaMovimiento(),
                entity.getObservacion(),
                entity.getReferenciaTipo(),
                entity.getReferenciaId(),
                detalles.stream()
                        .map(d -> new MovimientoDetalle(d.getProductoId(), d.getLoteId(), d.getCantidad()))
                        .toList());
    }

    public MovimientoEntity toEntity(Movimiento domain) {
        return MovimientoEntity.builder()
                .id(domain.getId())
                .tipoMovimiento(domain.getTipo().name())
                .depositoOrigenId(domain.getDepositoOrigenId())
                .depositoDestinoId(domain.getDepositoDestinoId())
                .usuarioId(domain.getUsuarioId())
                .fechaMovimiento(domain.getFecha())
                .observacion(domain.getObservacion())
                .referenciaTipo(domain.getReferenciaTipo())
                .referenciaId(domain.getReferenciaId())
                .build();
    }

    public MovimientoDetalleEntity toDetalleEntity(Long movimientoId, MovimientoDetalle detalle) {
        return MovimientoDetalleEntity.builder()
                .movimientoId(movimientoId)
                .productoId(detalle.productoId())
                .loteId(detalle.loteId())
                .cantidad(detalle.cantidad())
                .build();
    }
}

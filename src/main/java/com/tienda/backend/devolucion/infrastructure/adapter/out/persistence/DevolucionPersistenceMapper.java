package com.tienda.backend.devolucion.infrastructure.adapter.out.persistence;

import com.tienda.backend.devolucion.domain.model.Devolucion;
import com.tienda.backend.devolucion.domain.model.LineaDevuelta;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DevolucionPersistenceMapper {

    public Devolucion toDomain(DevolucionEntity e, List<DevolucionDetalleEntity> detalles) {
        List<LineaDevuelta> lineas = detalles.stream().map(d -> new LineaDevuelta(d.getId(), d.getVentaDetalleId(),
                d.getLoteId(), d.getCantidad(), d.isReingresaStock())).toList();
        return new Devolucion(e.getId(), e.getNumeroDevolucion(), e.getVentaId(), e.getUsuarioId(), e.getTurnoId(),
                e.getFecha(), e.getMotivo(), e.getMontoReembolsado(), e.getMetodoPagoId(), e.getVentaCambioId(),
                e.getIdempotencyKey(), e.getIdempotencyHash(), lineas);
    }

    public Devolucion toDomainCabecera(DevolucionEntity e) {
        return toDomain(e, List.of());
    }

    public DevolucionEntity toEntity(Devolucion d) {
        return DevolucionEntity.builder().id(d.getId()).ventaId(d.getVentaId()).usuarioId(d.getUsuarioId())
                .turnoId(d.getTurnoId()).numeroDevolucion(d.getNumeroDevolucion()).fecha(d.getFecha())
                .motivo(d.getMotivo()).montoReembolsado(d.getMontoReembolsado()).metodoPagoId(d.getMetodoPagoId())
                .ventaCambioId(d.getVentaCambioId()).idempotencyKey(d.getIdempotencyKey())
                .idempotencyHash(d.getIdempotencyHash()).build();
    }
}

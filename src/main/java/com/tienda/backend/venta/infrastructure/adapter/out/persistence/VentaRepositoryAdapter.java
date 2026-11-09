package com.tienda.backend.venta.infrastructure.adapter.out.persistence;

import com.tienda.backend.venta.application.model.VentaFiltro;
import com.tienda.backend.venta.application.port.out.VentaRepositoryPort;
import com.tienda.backend.venta.domain.model.LineaVenta;
import com.tienda.backend.venta.domain.model.LoteVendido;
import com.tienda.backend.venta.domain.model.PagoVenta;
import com.tienda.backend.venta.domain.model.Venta;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class VentaRepositoryAdapter implements VentaRepositoryPort {
    private final VentaJpaRepository ventaJpaRepository;
    private final VentaDetalleJpaRepository detalleJpaRepository;
    private final VentaDetalleLoteJpaRepository detalleLoteJpaRepository;
    private final VentaPagoJpaRepository pagoJpaRepository;
    private final VentaPersistenceMapper mapper;

    @Override
    public Venta save(Venta venta) {
        VentaEntity guardada = ventaJpaRepository.save(mapper.toEntity(venta));

        // Las líneas y pagos aún sin id son nuevos; los que ya tienen id no cambian (una venta no se edita)
        for (LineaVenta linea : venta.getLineas()) {
            if (linea.id() != null) {
                continue;
            }
            VentaDetalleEntity detalle = detalleJpaRepository.save(VentaDetalleEntity.builder()
                    .ventaId(guardada.getId()).productoId(linea.productoId()).presentacionId(linea.presentacionId())
                    .cantidad(linea.cantidad()).factorConversion(linea.factorConversion())
                    .precioUnitario(linea.precioUnitario()).descuento(linea.descuento()).subtotal(linea.subtotal())
                    .esPrecioModificado(linea.esPrecioModificado()).motivoPrecio(linea.motivoPrecio()).build());
            for (LoteVendido lote : linea.lotes()) {
                detalleLoteJpaRepository.save(VentaDetalleLoteEntity.builder().ventaDetalleId(detalle.getId())
                        .loteId(lote.loteId()).cantidad(lote.cantidadBase()).costoUnitario(lote.costoUnitario()).build());
            }
        }
        for (PagoVenta pago : venta.getPagos()) {
            if (pago.id() == null) {
                pagoJpaRepository.save(VentaPagoEntity.builder().ventaId(guardada.getId())
                        .metodoPagoId(pago.metodoPagoId()).monto(pago.monto()).referencia(pago.referencia()).build());
            }
        }
        return cargar(guardada);
    }

    @Override
    public Optional<Venta> findById(Long id) {
        return ventaJpaRepository.findById(id).map(this::cargar);
    }

    @Override
    public Optional<Venta> findByIdParaActualizar(Long id) {
        return ventaJpaRepository.findParaActualizar(id).map(this::cargar);
    }

    @Override
    public Optional<Venta> findByUsuarioIdAndIdempotencyKey(Long usuarioId, String idempotencyKey) {
        return ventaJpaRepository.findByUsuarioIdAndIdempotencyKey(usuarioId, idempotencyKey).map(this::cargar);
    }

    @Override
    public PageResult<Venta> findAll(VentaFiltro filtro, int page, int size) {
        Page<VentaEntity> result = ventaJpaRepository.findAll(VentaSpecifications.desde(filtro),
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("fechaVenta"), Sort.Order.desc("id"))));
        return new PageResult<>(result.getContent().stream().map(mapper::toDomainCabecera).toList(), result.getNumber(),
                result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    /** Trae las filas hijas de la cabecera con tres consultas (detalles, lotes de todos los detalles y pagos). */
    private Venta cargar(VentaEntity e) {
        List<VentaDetalleEntity> detalles = detalleJpaRepository.findByVentaIdOrderByIdAsc(e.getId());
        Map<Long, List<VentaDetalleLoteEntity>> lotes = detalles.isEmpty() ? Map.of()
                : detalleLoteJpaRepository.findByVentaDetalleIdInOrderByIdAsc(
                        detalles.stream().map(VentaDetalleEntity::getId).toList())
                        .stream().collect(Collectors.groupingBy(VentaDetalleLoteEntity::getVentaDetalleId));
        return mapper.toDomain(e, detalles, lotes, pagoJpaRepository.findByVentaIdOrderByIdAsc(e.getId()));
    }
}

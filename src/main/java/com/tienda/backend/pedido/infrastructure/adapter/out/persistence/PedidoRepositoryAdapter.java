package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import com.tienda.backend.pedido.application.model.PedidoFiltro;
import com.tienda.backend.pedido.application.port.out.PedidoRepositoryPort;
import com.tienda.backend.pedido.domain.model.LineaPedido;
import com.tienda.backend.pedido.domain.model.PedidoProveedor;
import com.tienda.backend.pedido.domain.model.Recepcion;
import com.tienda.backend.pedido.domain.model.Recepcion.LineaRecepcion;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PedidoRepositoryAdapter implements PedidoRepositoryPort {
    private final PedidoJpaRepository pedidoJpaRepository;
    private final PedidoDetalleJpaRepository detalleJpaRepository;
    private final RecepcionJpaRepository recepcionJpaRepository;
    private final RecepcionDetalleJpaRepository recepcionDetalleJpaRepository;
    private final PedidoPersistenceMapper mapper;

    @Override
    public PedidoProveedor save(PedidoProveedor pedido) {
        PedidoEntity guardado = pedidoJpaRepository.save(mapper.toEntity(pedido));

        // Las líneas que ya no están en el pedido (solo ocurre al editar un borrador) se borran; el resto se
        // inserta (sin id) o se actualiza (con id).
        Set<Long> idsActuales = new HashSet<>();
        for (LineaPedido linea : pedido.getLineas()) {
            if (linea.getId() != null) {
                idsActuales.add(linea.getId());
            }
        }
        List<PedidoDetalleEntity> sobrantes = detalleJpaRepository.findByPedidoIdOrderByIdAsc(guardado.getId()).stream()
                .filter(d -> !idsActuales.contains(d.getId())).toList();
        detalleJpaRepository.deleteAll(sobrantes);
        detalleJpaRepository.flush();
        for (LineaPedido linea : pedido.getLineas()) {
            detalleJpaRepository.save(mapper.toEntity(guardado.getId(), linea));
        }
        return cargar(guardado);
    }

    @Override
    public Optional<PedidoProveedor> findById(Long id) {
        return pedidoJpaRepository.findById(id).map(this::cargar);
    }

    @Override
    public Optional<PedidoProveedor> findByIdParaActualizar(Long id) {
        return pedidoJpaRepository.findParaActualizar(id).map(this::cargar);
    }

    @Override
    public PageResult<PedidoProveedor> findAll(PedidoFiltro filtro, int page, int size) {
        Page<PedidoEntity> result = pedidoJpaRepository.findAll(PedidoSpecifications.desde(filtro),
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("fechaPedido"), Sort.Order.desc("id"))));

        List<Long> ids = result.getContent().stream().map(PedidoEntity::getId).toList();
        Map<Long, List<PedidoDetalleEntity>> detalles = ids.isEmpty() ? Map.of()
                : detalleJpaRepository.findByPedidoIdInOrderByIdAsc(ids).stream()
                        .collect(Collectors.groupingBy(PedidoDetalleEntity::getPedidoId));
        List<PedidoProveedor> pedidos = result.getContent().stream()
                .map(e -> mapper.toDomain(e, detalles.getOrDefault(e.getId(), List.of())))
                .toList();
        return new PageResult<>(pedidos, result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public void deleteById(Long id) {
        pedidoJpaRepository.deleteById(id);
    }

    @Override
    public Recepcion saveRecepcion(Recepcion recepcion) {
        RecepcionEntity cabecera = recepcionJpaRepository.save(RecepcionEntity.builder()
                .pedidoId(recepcion.pedidoId()).usuarioId(recepcion.usuarioId())
                .fechaRecepcion(recepcion.fechaRecepcion()).numeroGuia(recepcion.numeroGuia())
                .observacion(recepcion.observacion()).build());

        List<RecepcionDetalleEntity> detalles = new ArrayList<>();
        for (LineaRecepcion linea : recepcion.lineas()) {
            detalles.add(recepcionDetalleJpaRepository.save(RecepcionDetalleEntity.builder()
                    .recepcionId(cabecera.getId()).pedidoDetalleId(linea.pedidoDetalleId())
                    .loteId(linea.loteId()).cantidad(linea.cantidad()).build()));
        }
        return mapper.toDomain(cabecera, detalles);
    }

    @Override
    public List<Recepcion> findRecepcionesByPedidoId(Long pedidoId) {
        List<RecepcionEntity> cabeceras = recepcionJpaRepository.findByPedidoIdOrderByIdAsc(pedidoId);
        if (cabeceras.isEmpty()) {
            return List.of();
        }
        Map<Long, List<RecepcionDetalleEntity>> detalles = recepcionDetalleJpaRepository
                .findByRecepcionIdInOrderByIdAsc(cabeceras.stream().map(RecepcionEntity::getId).toList()).stream()
                .collect(Collectors.groupingBy(RecepcionDetalleEntity::getRecepcionId));
        return cabeceras.stream()
                .map(c -> mapper.toDomain(c, detalles.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    private PedidoProveedor cargar(PedidoEntity e) {
        return mapper.toDomain(e, detalleJpaRepository.findByPedidoIdOrderByIdAsc(e.getId()));
    }
}

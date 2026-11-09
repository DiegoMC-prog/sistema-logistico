package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PedidoDetalleJpaRepository extends JpaRepository<PedidoDetalleEntity, Long> {
    List<PedidoDetalleEntity> findByPedidoIdOrderByIdAsc(Long pedidoId);

    List<PedidoDetalleEntity> findByPedidoIdInOrderByIdAsc(Collection<Long> pedidoIds);
}

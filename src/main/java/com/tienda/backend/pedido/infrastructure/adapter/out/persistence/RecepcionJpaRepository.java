package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecepcionJpaRepository extends JpaRepository<RecepcionEntity, Long> {
    List<RecepcionEntity> findByPedidoIdOrderByIdAsc(Long pedidoId);
}

package com.tienda.backend.pedido.infrastructure.adapter.out.persistence;

import com.tienda.backend.pedido.application.model.PedidoFiltro;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PedidoSpecifications {

    private PedidoSpecifications() {
    }

    public static Specification<PedidoEntity> desde(PedidoFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.numero() != null) {
                predicados.add(LikeSupport.containsIgnoreCase(cb, root.get("numeroPedido"), filtro.numero()));
            }
            if (filtro.proveedorId() != null) {
                predicados.add(cb.equal(root.get("proveedorId"), filtro.proveedorId()));
            }
            if (filtro.depositoId() != null) {
                predicados.add(cb.equal(root.get("depositoDestinoId"), filtro.depositoId()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            if (filtro.desde() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("fechaPedido"), filtro.desde()));
            }
            if (filtro.hasta() != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("fechaPedido"), filtro.hasta()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

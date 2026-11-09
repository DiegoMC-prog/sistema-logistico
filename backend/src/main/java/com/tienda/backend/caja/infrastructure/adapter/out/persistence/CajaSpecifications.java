package com.tienda.backend.caja.infrastructure.adapter.out.persistence;

import com.tienda.backend.caja.application.model.CajaFiltro;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class CajaSpecifications {

    private CajaSpecifications() {
    }

    public static Specification<CajaEntity> desde(CajaFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            if (filtro.search() != null) {
                predicados.add(LikeSupport.containsIgnoreCase(cb, root.get("nombre"), filtro.search()));
            }
            if (filtro.depositoId() != null) {
                predicados.add(cb.equal(root.get("depositoId"), filtro.depositoId()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

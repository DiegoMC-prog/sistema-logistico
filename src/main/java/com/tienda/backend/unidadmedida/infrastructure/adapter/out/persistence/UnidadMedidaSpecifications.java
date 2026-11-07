package com.tienda.backend.unidadmedida.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import com.tienda.backend.unidadmedida.application.model.UnidadMedidaFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UnidadMedidaSpecifications {

    private UnidadMedidaSpecifications() {
    }

    public static Specification<UnidadMedidaEntity> desde(UnidadMedidaFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.search() != null) {
                predicados.add(cb.or(
                        LikeSupport.containsIgnoreCase(cb, root.get("nombre"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("abreviatura"), filtro.search())));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

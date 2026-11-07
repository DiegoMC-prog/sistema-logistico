package com.tienda.backend.categoria.infrastructure.adapter.out.persistence;

import com.tienda.backend.categoria.application.model.CategoriaFiltro;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class CategoriaSpecifications {

    private CategoriaSpecifications() {
    }

    public static Specification<CategoriaEntity> desde(CategoriaFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.search() != null) {
                predicados.add(LikeSupport.containsIgnoreCase(cb, root.get("nombre"), filtro.search()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

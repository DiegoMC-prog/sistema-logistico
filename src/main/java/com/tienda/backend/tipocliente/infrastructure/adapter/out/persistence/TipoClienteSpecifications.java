package com.tienda.backend.tipocliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import com.tienda.backend.tipocliente.application.model.TipoClienteFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TipoClienteSpecifications {

    private TipoClienteSpecifications() {
    }

    public static Specification<TipoClienteEntity> desde(TipoClienteFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.search() != null) {
                predicados.add(LikeSupport.containsIgnoreCase(cb, root.get("nombre"), filtro.search()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

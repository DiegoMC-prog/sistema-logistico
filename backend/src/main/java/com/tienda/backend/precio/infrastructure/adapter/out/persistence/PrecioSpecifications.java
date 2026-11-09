package com.tienda.backend.precio.infrastructure.adapter.out.persistence;

import com.tienda.backend.precio.application.model.PrecioFiltro;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PrecioSpecifications {

    private PrecioSpecifications() {
    }

    public static Specification<PrecioEntity> desde(Long presentacionId, PrecioFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.equal(root.get("presentacionId"), presentacionId));

            if (filtro.tipoClienteId() != null) {
                predicados.add(cb.equal(root.get("tipoClienteId"), filtro.tipoClienteId()));
            }
            if (Boolean.TRUE.equals(filtro.soloGenerales())) {
                predicados.add(cb.isNull(root.get("tipoClienteId")));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

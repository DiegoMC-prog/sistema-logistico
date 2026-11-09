package com.tienda.backend.lote.infrastructure.adapter.out.persistence;

import com.tienda.backend.lote.application.model.LoteFiltro;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class LoteSpecifications {

    private LoteSpecifications() {
    }

    public static Specification<LoteEntity> desde(LoteFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.productoId() != null) {
                predicados.add(cb.equal(root.get("producto").get("id"), filtro.productoId()));
            }
            if (filtro.search() != null) {
                predicados.add(LikeSupport.containsIgnoreCase(cb, root.get("numeroLote"), filtro.search()));
            }
            if (filtro.venceDesde() != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("fechaVencimiento"), filtro.venceDesde()));
            }
            if (filtro.venceHasta() != null) {
                predicados.add(cb.lessThanOrEqualTo(root.<LocalDate>get("fechaVencimiento"), filtro.venceHasta()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

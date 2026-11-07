package com.tienda.backend.usuario.infrastructure.adapter.out.persistence;

import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import com.tienda.backend.usuario.application.model.UsuarioFiltro;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    public static Specification<UsuarioEntity> desde(UsuarioFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.search() != null) {
                predicados.add(cb.or(
                        LikeSupport.containsIgnoreCase(cb, root.get("username"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("nombre"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("email"), filtro.search())));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }
            if (filtro.rolId() != null) {
                Subquery<Long> asignaciones = query.subquery(Long.class);
                Root<UsuarioRolEntity> ur = asignaciones.from(UsuarioRolEntity.class);
                asignaciones.select(ur.<Long>get("id")).where(
                        cb.equal(ur.get("usuarioId"), root.get("id")),
                        cb.equal(ur.get("rolId"), filtro.rolId()));
                predicados.add(cb.exists(asignaciones));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

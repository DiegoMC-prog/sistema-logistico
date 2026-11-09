package com.tienda.backend.cliente.infrastructure.adapter.out.persistence;

import com.tienda.backend.cliente.application.model.ClienteFiltro;
import com.tienda.backend.shared.infrastructure.adapter.out.persistence.LikeSupport;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ClienteSpecifications {

    private ClienteSpecifications() {
    }

    public static Specification<ClienteEntity> desde(ClienteFiltro filtro) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            if (filtro.search() != null) {
                predicados.add(cb.or(
                        LikeSupport.containsIgnoreCase(cb, root.get("nombre"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("documento"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("email"), filtro.search()),
                        LikeSupport.containsIgnoreCase(cb, root.get("telefono"), filtro.search())));
            }
            if (filtro.tipoClienteId() != null) {
                predicados.add(cb.equal(root.get("tipoCliente").get("id"), filtro.tipoClienteId()));
            }
            if (filtro.estado() != null) {
                predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}

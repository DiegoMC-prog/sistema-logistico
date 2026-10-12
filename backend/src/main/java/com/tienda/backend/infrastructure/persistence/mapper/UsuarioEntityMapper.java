package com.tienda.backend.infrastructure.persistence.mapper;

import com.tienda.backend.domain.model.Usuario;
import com.tienda.backend.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioEntityMapper {

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) {
            return null;
        }

        return new UsuarioEntity(
                domain.getId(),
                domain.getUsername(),
                domain.getEmail(),
                domain.getPassword(),
                domain.getNombre(),
                domain.getApellido(),
                domain.getRol(),
                domain.getEstado()
        );
    }

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Usuario(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getNombre(),
                entity.getApellido(),
                entity.getRol(),
                entity.getEstado()
        );
    }
}

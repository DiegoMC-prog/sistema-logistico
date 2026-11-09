package com.tienda.backend.usuario.infrastructure.adapter.out.persistence;

import com.tienda.backend.usuario.domain.model.EstadoUsuario;
import com.tienda.backend.usuario.domain.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPersistenceMapper {
    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;

        return new Usuario(
                entity.getId(),
                entity.getUsername(),
                entity.getNombre(),
                entity.getEmail(),
                EstadoUsuario.valueOf(entity.getEstado()),
                entity.getPassword(),
                entity.isPasswordTemporal()
        );
    }

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;

        return UsuarioEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .nombre(domain.getNombre())
                .email(domain.getEmail())
                .password(domain.getPasswordHash())
                .passwordTemporal(domain.isPasswordTemporal())
                .estado(domain.getEstado().name())
                .build();
    }
}

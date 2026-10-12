package com.tienda.backend.application.mapper;

import com.tienda.backend.application.dto.request.RegisterRequest;
import com.tienda.backend.application.dto.response.UsuarioResponse;
import com.tienda.backend.domain.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioDtoMapper {

    public Usuario toDomain(RegisterRequest request, String passwordHasheada) {
        return Usuario.crear(
                request.username(),
                request.email(),
                passwordHasheada,
                request.nombre(),
                request.apellido(),
                request.rol()
        );
    }

    public UsuarioResponse toResponse(Usuario domain) {
        if (domain == null) {
            return null;
        }

        return new UsuarioResponse(
                domain.getId(),
                domain.getUsername(),
                domain.getEmail(),
                domain.getNombre(),
                domain.getApellido(),
                domain.getRol(),
                domain.getEstado()
        );
    }
}

package com.tienda.backend.rol.infrastructure.adapter.out.persistence;

import com.tienda.backend.rol.application.port.out.UsuarioConsultaPort;
import com.tienda.backend.usuario.application.port.out.UsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioConsultaAdapter implements UsuarioConsultaPort {
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    public boolean existeUsuarioConRol(Long rolId) {
        return usuarioRepositoryPort.existsUsuarioConRol(rolId);
    }
}

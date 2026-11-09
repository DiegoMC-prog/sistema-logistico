package com.tienda.backend.shared.infrastructure.adapter.out.security;

import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.UsuarioNoIdentificadoException;
import com.tienda.backend.shared.infrastructure.config.security.UsuarioActual;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextUsuarioActualAdapter implements UsuarioActualPort {

    @Override
    public Long id() {
        return UsuarioActual.id().orElseThrow(UsuarioNoIdentificadoException::new);
    }

    @Override
    public boolean tienePermiso(String permiso) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .anyMatch(a -> permiso.equals(a.getAuthority()));
    }

    @Override
    public String nombre() {
        return UsuarioActual.nombre();
    }
}

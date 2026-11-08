package com.tienda.backend.shared.infrastructure.adapter.out.security;

import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.infrastructure.config.security.UsuarioActual;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextUsuarioActualAdapter implements UsuarioActualPort {

    @Override
    public String nombre() {
        return UsuarioActual.nombre();
    }
}

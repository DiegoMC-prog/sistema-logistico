package com.tienda.backend.inventario.infrastructure.adapter.out.consulta;

import com.tienda.backend.inventario.application.port.out.UsuarioConsultaPort;
import com.tienda.backend.usuario.application.port.out.UsuarioRepositoryPort;
import com.tienda.backend.usuario.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InventarioUsuarioConsultaAdapter implements UsuarioConsultaPort {
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    public List<Usuario> obtenerPorIds(Collection<Long> usuarioIds) {
        return usuarioIds.isEmpty() ? List.of() : usuarioRepositoryPort.findAllById(usuarioIds);
    }
}

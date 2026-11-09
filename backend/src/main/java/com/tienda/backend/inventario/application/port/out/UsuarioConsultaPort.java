package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.usuario.domain.model.Usuario;

import java.util.Collection;
import java.util.List;

public interface UsuarioConsultaPort {
    List<Usuario> obtenerPorIds(Collection<Long> usuarioIds);
}

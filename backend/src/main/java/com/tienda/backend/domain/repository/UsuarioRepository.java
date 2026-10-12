package com.tienda.backend.domain.repository;

import com.tienda.backend.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UsuarioRepository {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorId(Long id);
    Optional<Usuario> buscarPorUsername(String username);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorUsernameOEmail(String usernameOEmail);
    boolean existePorUsername(String username);
    boolean existePorEmail(String email);
    Page<Usuario> listarTodos(Pageable pageable);
    void eliminar(Long id);
}

package com.tienda.backend.usuario.application.port.out;

import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.usuario.application.model.UsuarioFiltro;
import com.tienda.backend.usuario.domain.model.Usuario;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UsuarioRepositoryPort {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAllById(Collection<Long> ids);
    Optional<Usuario> findByUsername(String username);
    List<Long> findUsuarioIdsByRolId(Long rolId);
    PageResult<Usuario> findAll(UsuarioFiltro filtro, int page, int size);
    void deleteById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsUsuarioConRol(Long rolId);
    Set<Long> findRolIdsByUsuarioId(Long usuarioId);
    Map<Long, Set<Long>> findRolIdsByUsuarioIds(Collection<Long> usuarioIds);
    void reemplazarRoles(Long usuarioId, Set<Long> rolIds, String asignadoPor);
    boolean tieneRolAdministrador(Long usuarioId);
    long contarAdministradoresActivos();
}

package com.tienda.backend.usuario.infrastructure.adapter.out.persistence;

import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.usuario.application.model.UsuarioFiltro;
import com.tienda.backend.usuario.application.port.out.UsuarioRepositoryPort;
import com.tienda.backend.usuario.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final UsuarioRolJpaRepository usuarioRolJpaRepository;
    private final UsuarioPersistenceMapper mapper;

    @Override
    public Usuario save(Usuario usuario) {
        return mapper.toDomain(usuarioJpaRepository.save(mapper.toEntity(usuario)));
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<Usuario> findAll(UsuarioFiltro filtro, int page, int size) {
        Page<UsuarioEntity> result = usuarioJpaRepository.findAll(
                UsuarioSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public void deleteById(Long id) {
        usuarioJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return usuarioJpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return usuarioJpaRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return usuarioJpaRepository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public boolean existsUsuarioConRol(Long rolId) {
        return usuarioRolJpaRepository.contarUsuariosConRol(rolId) > 0;
    }

    @Override
    public Set<Long> findRolIdsByUsuarioId(Long usuarioId) {
        return usuarioRolJpaRepository.findByUsuarioId(usuarioId).stream()
                .map(UsuarioRolEntity::getRolId)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<Long, Set<Long>> findRolIdsByUsuarioIds(Collection<Long> usuarioIds) {
        return usuarioRolJpaRepository.findByUsuarioIdIn(usuarioIds).stream()
                .collect(Collectors.groupingBy(
                        UsuarioRolEntity::getUsuarioId,
                        Collectors.mapping(UsuarioRolEntity::getRolId, Collectors.toSet())));
    }

    @Override
    public void reemplazarRoles(Long usuarioId, Set<Long> rolIds, String asignadoPor) {
        List<UsuarioRolEntity> actuales = usuarioRolJpaRepository.findByUsuarioId(usuarioId);

        List<UsuarioRolEntity> sobrantes = actuales.stream()
                .filter(e -> !rolIds.contains(e.getRolId()))
                .toList();
        Set<Long> yaAsignados = actuales.stream().map(UsuarioRolEntity::getRolId).collect(Collectors.toSet());
        LocalDateTime ahora = LocalDateTime.now();
        List<UsuarioRolEntity> nuevos = rolIds.stream()
                .filter(id -> !yaAsignados.contains(id))
                .map(id -> UsuarioRolEntity.builder()
                        .usuarioId(usuarioId)
                        .rolId(id)
                        .assignedBy(asignadoPor)
                        .assignedAt(ahora)
                        .build())
                .toList();

        usuarioRolJpaRepository.deleteAll(sobrantes);
        usuarioRolJpaRepository.saveAll(nuevos);
    }

    @Override
    public boolean tieneRolAdministrador(Long usuarioId) {
        return usuarioRolJpaRepository.contarAsignacionesDeRol(usuarioId, Rol.NOMBRE_ADMIN) > 0;
    }

    @Override
    public long contarAdministradoresActivos() {
        return usuarioRolJpaRepository.contarUsuariosActivosConRol(Rol.NOMBRE_ADMIN);
    }
}

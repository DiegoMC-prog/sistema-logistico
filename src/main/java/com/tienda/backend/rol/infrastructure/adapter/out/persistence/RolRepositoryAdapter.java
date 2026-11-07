package com.tienda.backend.rol.infrastructure.adapter.out.persistence;

import com.tienda.backend.rol.application.model.RolFiltro;
import com.tienda.backend.rol.application.port.out.RolRepositoryPort;
import com.tienda.backend.rol.domain.model.Rol;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class RolRepositoryAdapter implements RolRepositoryPort {
    private final RolJpaRepository rolJpaRepository;
    private final RolPermisoJpaRepository rolPermisoJpaRepository;
    private final RolPersistenceMapper mapper;

    @Override
    public Rol save(Rol rol) {
        return mapper.toDomain(rolJpaRepository.save(mapper.toEntity(rol)));
    }

    @Override
    public Optional<Rol> findById(Long id) {
        return rolJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Rol> findAllById(Collection<Long> ids) {
        return rolJpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public PageResult<Rol> findAll(RolFiltro filtro, int page, int size) {
        Page<RolEntity> result = rolJpaRepository.findAll(
                RolSpecifications.desde(filtro), PageRequest.of(page, size, Sort.by("id")));
        return new PageResult<>(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public void deleteById(Long id) {
        rolJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return rolJpaRepository.existsByNombre(nombre);
    }

    @Override
    public boolean existsByNombreAndIdNot(String nombre, Long id) {
        return rolJpaRepository.existsByNombreAndIdNot(nombre, id);
    }

    @Override
    public Set<Long> findPermisoIdsByRolId(Long rolId) {
        return rolPermisoJpaRepository.findByRolId(rolId).stream()
                .map(RolPermisoEntity::getPermisoId)
                .collect(Collectors.toSet());
    }

    @Override
    public void reemplazarPermisos(Long rolId, Set<Long> permisoIds) {
        List<RolPermisoEntity> actuales = rolPermisoJpaRepository.findByRolId(rolId);

        List<RolPermisoEntity> sobrantes = actuales.stream()
                .filter(e -> !permisoIds.contains(e.getPermisoId()))
                .toList();
        Set<Long> yaAsignados = actuales.stream().map(RolPermisoEntity::getPermisoId).collect(Collectors.toSet());
        List<RolPermisoEntity> nuevos = permisoIds.stream()
                .filter(id -> !yaAsignados.contains(id))
                .map(id -> RolPermisoEntity.builder().rolId(rolId).permisoId(id).build())
                .toList();

        rolPermisoJpaRepository.deleteAll(sobrantes);
        rolPermisoJpaRepository.saveAll(nuevos);
    }
}

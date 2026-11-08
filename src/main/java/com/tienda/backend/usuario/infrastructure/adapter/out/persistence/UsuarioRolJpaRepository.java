package com.tienda.backend.usuario.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface UsuarioRolJpaRepository extends JpaRepository<UsuarioRolEntity, Long> {

    List<UsuarioRolEntity> findByUsuarioId(Long usuarioId);

    List<UsuarioRolEntity> findByUsuarioIdIn(Collection<Long> usuarioIds);

    /** Ids de los usuarios (no eliminados) que tienen el rol. */
    @Query("select ur.usuarioId from UsuarioRolEntity ur, UsuarioEntity u "
            + "where ur.rolId = :rolId and ur.usuarioId = u.id")
    List<Long> usuarioIdsConRol(@Param("rolId") Long rolId);

    /** Cuenta asignaciones del rol a usuarios no eliminados (el borrado lógico filtra a los eliminados). */
    @Query("select count(ur) from UsuarioRolEntity ur, UsuarioEntity u "
            + "where ur.rolId = :rolId and ur.usuarioId = u.id")
    long contarUsuariosConRol(@Param("rolId") Long rolId);

    @Query("select count(ur) from UsuarioRolEntity ur, RolEntity r "
            + "where ur.usuarioId = :usuarioId and ur.rolId = r.id and r.nombre = :nombreRol")
    long contarAsignacionesDeRol(@Param("usuarioId") Long usuarioId, @Param("nombreRol") String nombreRol);

    @Query("select count(distinct u.id) from UsuarioEntity u, UsuarioRolEntity ur, RolEntity r "
            + "where u.estado = 'ACTIVO' and ur.usuarioId = u.id and ur.rolId = r.id and r.nombre = :nombreRol")
    long contarUsuariosActivosConRol(@Param("nombreRol") String nombreRol);
}

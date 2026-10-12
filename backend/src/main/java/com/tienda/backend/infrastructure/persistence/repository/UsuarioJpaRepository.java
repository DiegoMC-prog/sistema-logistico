package com.tienda.backend.infrastructure.persistence.repository;

import com.tienda.backend.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByUsername(String username);

    Optional<UsuarioEntity> findByEmail(String email);

    @Query("SELECT u FROM UsuarioEntity u WHERE LOWER(u.username) = LOWER(:identificador) OR LOWER(u.email) = LOWER(:identificador)")
    Optional<UsuarioEntity> findByUsernameOrEmail(@Param("identificador") String identificador);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

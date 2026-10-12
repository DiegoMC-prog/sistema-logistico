package com.tienda.backend.infrastructure.persistence.adapter;

import com.tienda.backend.domain.model.Usuario;
import com.tienda.backend.domain.repository.UsuarioRepository;
import com.tienda.backend.infrastructure.persistence.entity.UsuarioEntity;
import com.tienda.backend.infrastructure.persistence.mapper.UsuarioEntityMapper;
import com.tienda.backend.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final UsuarioEntityMapper mapper;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository usuarioJpaRepository, UsuarioEntityMapper mapper) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = mapper.toEntity(usuario);
        UsuarioEntity guardado = usuarioJpaRepository.save(entity);
        return mapper.toDomain(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioJpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioJpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorUsernameOEmail(String usernameOEmail) {
        return usuarioJpaRepository.findByUsernameOrEmail(usernameOEmail).map(mapper::toDomain);
    }

    @Override
    public boolean existePorUsername(String username) {
        return usuarioJpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existePorEmail(String email) {
        return usuarioJpaRepository.existsByEmail(email);
    }

    @Override
    public Page<Usuario> listarTodos(Pageable pageable) {
        return usuarioJpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public void eliminar(Long id) {
        usuarioJpaRepository.deleteById(id);
    }
}

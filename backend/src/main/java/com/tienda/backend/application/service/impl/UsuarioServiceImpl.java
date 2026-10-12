package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.ActualizarUsuarioRequest;
import com.tienda.backend.application.dto.request.CambiarPasswordRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.UsuarioResponse;
import com.tienda.backend.application.mapper.UsuarioDtoMapper;
import com.tienda.backend.application.service.UsuarioService;
import com.tienda.backend.domain.exception.RecursoNoEncontrado;
import com.tienda.backend.domain.model.Usuario;
import com.tienda.backend.domain.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioDtoMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
            UsuarioDtoMapper mapper,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el usuario con ID " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorUsername(String username) {
        return usuarioRepository.buscarPorUsername(username.trim().toLowerCase())
                .map(mapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el usuario con username " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("El parámetro 'page' debe ser mayor o igual a 0.");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El parámetro 'size' debe estar entre 1 y 100.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        return PageResponse.from(usuarioRepository.listarTodos(pageable).map(mapper::toResponse));
    }

    @Override
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el usuario con ID " + id));

        if (!usuario.getEmail().equalsIgnoreCase(request.email().trim())
                && usuarioRepository.existePorEmail(request.email().trim().toLowerCase())) {
            throw new IllegalArgumentException(
                    "El correo electrónico '" + request.email().trim() + "' ya está en uso por otro usuario.");
        }

        usuario.actualizarPerfil(request.nombre(), request.apellido(), request.email());
        usuario.cambiarRol(request.rol());

        Usuario actualizado = usuarioRepository.guardar(usuario);
        return mapper.toResponse(actualizado);
    }

    @Override
    public void cambiarPassword(Long id, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el usuario con ID " + id));

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }

        String nuevaPasswordHasheada = passwordEncoder.encode(request.nuevaPassword());
        usuario.cambiarPassword(nuevaPasswordHasheada);
        usuarioRepository.guardar(usuario);
    }

    @Override
    public UsuarioResponse cambiarEstado(Long id, boolean estado) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontrado("No se encontró el usuario con ID " + id));

        usuario.cambiarEstado(estado);
        Usuario actualizado = usuarioRepository.guardar(usuario);
        return mapper.toResponse(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        if (usuarioRepository.buscarPorId(id).isEmpty()) {
            throw new RecursoNoEncontrado("No se puede eliminar: no existe el usuario con ID " + id);
        }
        usuarioRepository.eliminar(id);
    }
}

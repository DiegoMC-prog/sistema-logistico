package com.tienda.backend.application.service.impl;

import com.tienda.backend.application.dto.request.LoginRequest;
import com.tienda.backend.application.dto.request.RegisterRequest;
import com.tienda.backend.application.dto.response.AuthResponse;
import com.tienda.backend.application.dto.response.UsuarioResponse;
import com.tienda.backend.application.mapper.UsuarioDtoMapper;
import com.tienda.backend.application.service.AuthService;
import com.tienda.backend.domain.exception.RecursoNoEncontrado;
import com.tienda.backend.domain.model.Usuario;
import com.tienda.backend.domain.repository.UsuarioRepository;
import com.tienda.backend.infrastructure.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioDtoMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           UsuarioDtoMapper mapper,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail().trim(), request.password())
        );

        Usuario usuario = usuarioRepository.buscarPorUsernameOEmail(request.usernameOrEmail().trim())
                .orElseThrow(() -> new RecursoNoEncontrado("Usuario no encontrado con: " + request.usernameOrEmail()));

        String token = tokenProvider.generarToken(usuario);

        return AuthResponse.of(
                token,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getRol(),
                tokenProvider.getExpirationMs()
        );
    }

    @Override
    public UsuarioResponse register(RegisterRequest request) {
        if (usuarioRepository.existePorUsername(request.username().trim().toLowerCase())) {
            throw new IllegalArgumentException("El nombre de usuario '" + request.username().trim() + "' ya está registrado.");
        }

        if (usuarioRepository.existePorEmail(request.email().trim().toLowerCase())) {
            throw new IllegalArgumentException("El correo electrónico '" + request.email().trim() + "' ya está registrado.");
        }

        String passwordHasheada = passwordEncoder.encode(request.password());
        Usuario nuevoUsuario = mapper.toDomain(request, passwordHasheada);
        Usuario guardado = usuarioRepository.guardar(nuevoUsuario);

        return mapper.toResponse(guardado);
    }
}

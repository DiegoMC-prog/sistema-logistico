package com.tienda.backend.infrastructure.security;

import com.tienda.backend.domain.model.Usuario;
import com.tienda.backend.domain.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.buscarPorUsernameOEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No se encontró el usuario con username o email: " + usernameOrEmail));

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                Boolean.TRUE.equals(usuario.getEstado()),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(usuario.getRol().name()))
        );
    }
}

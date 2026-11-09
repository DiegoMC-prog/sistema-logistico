package com.tienda.backend.auth.infrastructure.security;

import com.tienda.backend.auth.application.model.ClaimsAcceso;
import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.application.port.out.TokenPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Autentica cada petición con el access token JWT, sin consultar la BD: la identidad, los roles y los permisos
 * viajan firmados en el token. Solo consulta la lista de revocados (en memoria o Redis).
 *
 * <p>Si no hay token, o es inválido, expirado o revocado, la petición sigue sin autenticar y las reglas de
 * {@code SecurityConfig} responden 401 en las rutas protegidas. No es un bean: lo crea {@code SecurityConfig} para
 * que Spring Boot no lo registre además como filtro global del servlet.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIJO_BEARER = "Bearer ";
    /** Con contraseña temporal solo se permiten estas rutas (cambiar la contraseña, ver el perfil, cerrar sesión). */
    private static final String PREFIJO_AUTH = "/api/v1/auth/";

    private final TokenPort tokenPort;
    private final RevocacionTokensPort revocacionTokensPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO_BEARER)) {
            Optional<ClaimsAcceso> claims = tokenPort.leerAcceso(cabecera.substring(PREFIJO_BEARER.length()).trim())
                    .filter(c -> !revocacionTokensPort.estaRevocado(c.usuarioId(), c.emitidoEnMs()));

            if (claims.isPresent()) {
                ClaimsAcceso c = claims.get();

                if (c.passwordTemporal() && !request.getRequestURI().startsWith(PREFIJO_AUTH)) {
                    RespuestaSeguridad.escribir(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                            "Debe cambiar su contraseña temporal antes de continuar.");
                    return;
                }

                SecurityContextHolder.getContext().setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                new UsuarioAutenticado(c.usuarioId(), c.username()), null, autoridades(c)));
            }
        }

        chain.doFilter(request, response);
    }

    private List<GrantedAuthority> autoridades(ClaimsAcceso claims) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        claims.roles().forEach(rol -> autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol)));
        claims.permisos().forEach(permiso -> autoridades.add(new SimpleGrantedAuthority(permiso)));
        return autoridades;
    }
}

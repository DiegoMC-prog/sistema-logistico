package com.tienda.backend.shared.infrastructure.config.security;

import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.application.port.out.TokenPort;
import com.tienda.backend.auth.infrastructure.config.JwtProperties;
import com.tienda.backend.auth.infrastructure.security.JwtAuthenticationFilter;
import com.tienda.backend.auth.infrastructure.security.RespuestaSeguridad;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static com.tienda.backend.shared.infrastructure.config.security.Permisos.CATALOGO_GESTIONAR;
import static com.tienda.backend.shared.infrastructure.config.security.Permisos.DEPOSITO_GESTIONAR;
import static com.tienda.backend.shared.infrastructure.config.security.Permisos.INVENTARIO_VER;
import static com.tienda.backend.shared.infrastructure.config.security.Permisos.ROL_GESTIONAR;
import static com.tienda.backend.shared.infrastructure.config.security.Permisos.USUARIO_GESTIONAR;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /** Catálogo: cualquier usuario autenticado lo consulta (caja y ventas lo necesitan); modificarlo exige CATALOGO_GESTIONAR. */
    private static final String[] CATALOGO = {
            "/api/v1/categorias/**",
            "/api/v1/productos/**",
            "/api/v1/unidades-medida/**",
            "/api/v1/presentaciones/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenPort tokenPort,
                                                   RevocacionTokensPort revocacionTokensPort) throws Exception {
        http
                // Sin sesión ni cookies: CSRF no aplica, la identidad viaja en el token Bearer
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                // Política de autorización completa en un solo lugar. Gana la PRIMERA regla que coincide.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // Autenticación: solo login, refresh y logout son públicos
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout").permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated()

                        // Usuarios, roles y permisos. Para asignar roles a un usuario hay que poder listarlos.
                        .requestMatchers("/api/v1/usuarios/**").hasAuthority(USUARIO_GESTIONAR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles/**", "/api/v1/permisos/**")
                                .hasAnyAuthority(USUARIO_GESTIONAR, ROL_GESTIONAR)
                        .requestMatchers("/api/v1/roles/**", "/api/v1/permisos/**").hasAuthority(ROL_GESTIONAR)

                        // Lotes: llevan costos, así que ni la lectura es para cualquiera
                        .requestMatchers(HttpMethod.GET, "/api/v1/lotes/**")
                                .hasAnyAuthority(CATALOGO_GESTIONAR, INVENTARIO_VER)
                        .requestMatchers("/api/v1/lotes/**").hasAuthority(CATALOGO_GESTIONAR)

                        // Catálogo y depósitos: lectura para cualquier autenticado, escritura con su permiso
                        .requestMatchers(HttpMethod.GET, CATALOGO).authenticated()
                        .requestMatchers(CATALOGO).hasAuthority(CATALOGO_GESTIONAR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/depositos/**").authenticated()
                        .requestMatchers("/api/v1/depositos/**").hasAuthority(DEPOSITO_GESTIONAR)

                        // Denegar por defecto: un módulo nuevo debe declarar aquí su permiso antes de quedar accesible
                        .requestMatchers("/api/v1/**").denyAll()
                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((request, response, ex) -> RespuestaSeguridad.escribir(
                                response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                                "Autenticación requerida o token inválido."))
                        .accessDeniedHandler((request, response, ex) -> RespuestaSeguridad.escribir(
                                response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                                "No tiene permiso para esta operación."))
                )

                .addFilterBefore(new JwtAuthenticationFilter(tokenPort, revocacionTokensPort),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS para un frontend en otro origen. Los orígenes permitidos salen de {@code app.cors.origenes}
     * (variable CORS_ORIGENES, separados por coma). Sin credenciales/cookies: el token va en la cabecera Authorization.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") List<String> origenes) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes.stream().map(String::trim).filter(o -> !o.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", config);
        return fuente;
    }
}

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

import static com.tienda.backend.shared.infrastructure.config.security.Permisos.*;

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

    /** Catálogo: cualquier usuario autenticado lo consulta (caja y ventas lo necesitan); modificarlo exige los permisos CATALOGO_*. */
    private static final String[] CATALOGO = {
            "/api/v1/categorias/**",
            "/api/v1/productos/**",
            "/api/v1/unidades-medida/**",
            "/api/v1/presentaciones/**"
    };

    /** Precios: cuelgan de /presentaciones/**, pero llevan su propio permiso. */
    private static final String[] PRECIOS = {
            "/api/v1/precios/**",
            "/api/v1/presentaciones/*/precios",
            "/api/v1/presentaciones/*/precio-aplicable"
    };

    private static final String[] CLIENTES = {
            "/api/v1/clientes/**",
            "/api/v1/tipos-cliente/**"
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

                        // Usuarios. Las acciones especiales van antes que el CRUD genérico de /usuarios/**.
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/usuarios/*/activar", "/api/v1/usuarios/*/desactivar")
                                .hasAuthority(USUARIO_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/*/restablecer-password")
                                .hasAuthority(USUARIO_RESTABLECER_PASSWORD)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/*/roles").hasAuthority(USUARIO_ASIGNAR_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/v1/usuarios/**").hasAuthority(USUARIO_VER)
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios").hasAuthority(USUARIO_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/usuarios/*").hasAuthority(USUARIO_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/usuarios/*").hasAuthority(USUARIO_ELIMINAR)

                        // Roles y permisos. Para asignar roles a un usuario hay que poder listar los roles; para
                        // armar un rol, listar los permisos.
                        .requestMatchers(HttpMethod.GET, "/api/v1/roles/**")
                                .hasAnyAuthority(ROL_VER, USUARIO_CREAR, USUARIO_EDITAR, USUARIO_ASIGNAR_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/v1/permisos/**")
                                .hasAnyAuthority(ROL_VER, ROL_CREAR, ROL_EDITAR, ROL_ASIGNAR_PERMISOS)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/roles/*/permisos").hasAuthority(ROL_ASIGNAR_PERMISOS)
                        .requestMatchers(HttpMethod.POST, "/api/v1/roles").hasAuthority(ROL_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/roles/*").hasAuthority(ROL_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/roles/*").hasAuthority(ROL_ELIMINAR)

                        // Lotes (parte del catálogo): llevan costos, así que ni la lectura es para cualquiera
                        .requestMatchers(HttpMethod.GET, "/api/v1/lotes/**")
                                .hasAnyAuthority(CATALOGO_EDITAR, INVENTARIO_VER)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/lotes/*").hasAuthority(CATALOGO_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/lotes/*").hasAuthority(CATALOGO_ELIMINAR)

                        // Caja: cada operación con su permiso. El servicio además restringe cada turno a su dueño
                        // (salvo CAJA_SUPERVISAR, que supervisa). Las rutas específicas van antes que /cajas/**.
                        .requestMatchers(HttpMethod.POST, "/api/v1/cajas/*/turnos").hasAuthority(CAJA_ABRIR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/cajas/*/turno-actual", "/api/v1/turnos/**")
                                .hasAnyAuthority(CAJA_ABRIR, CAJA_CERRAR, CAJA_MOVIMIENTO, CAJA_SUPERVISAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/turnos/*/cerrar").hasAuthority(CAJA_CERRAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/turnos/*/movimientos").hasAuthority(CAJA_MOVIMIENTO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/cajas/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/cajas/*/activar", "/api/v1/cajas/*/desactivar")
                                .hasAuthority(CAJA_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/cajas").hasAuthority(CAJA_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cajas/*").hasAuthority(CAJA_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cajas/*").hasAuthority(CAJA_ELIMINAR)

                        // Devoluciones y cambios: registrar con VENTA_DEVOLVER; consultar con VENTA_VER o VENTA_DEVOLVER (el servicio
                        // restringe a las propias a quien no tiene VENTA_VER). Antes de las reglas de /ventas/** que las taparían.
                        .requestMatchers(HttpMethod.POST, "/api/v1/ventas/*/devoluciones", "/api/v1/ventas/*/cambios")
                                .hasAuthority(VENTA_DEVOLVER)
                        .requestMatchers(HttpMethod.GET, "/api/v1/ventas/*/devoluciones", "/api/v1/devoluciones/**")
                                .hasAnyAuthority(VENTA_VER, VENTA_DEVOLVER)

                        // Ventas: crear y anular con su permiso; consultar con VENTA_VER (o VENTA_CREAR para ver las propias,
                        // que el servicio restringe a su autor)
                        .requestMatchers(HttpMethod.POST, "/api/v1/ventas").hasAuthority(VENTA_CREAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/ventas/*/anular").hasAuthority(VENTA_ANULAR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/ventas/**").hasAnyAuthority(VENTA_VER, VENTA_CREAR)

                        // Precios: caja los consulta (incluido el precio aplicable); escribir exige permiso de PRECIO.
                        // Va antes del catálogo porque estas rutas cuelgan de /presentaciones/**.
                        .requestMatchers(HttpMethod.GET, PRECIOS).authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/precios/*/activar", "/api/v1/precios/*/desactivar")
                                .hasAuthority(PRECIO_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, PRECIOS).hasAuthority(PRECIO_CREAR)
                        .requestMatchers(HttpMethod.PUT, PRECIOS).hasAuthority(PRECIO_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, PRECIOS).hasAuthority(PRECIO_ELIMINAR)

                        // Catálogo y depósitos: lectura para cualquier autenticado, escritura con su permiso
                        .requestMatchers(HttpMethod.GET, CATALOGO).authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/productos/*/activar", "/api/v1/productos/*/desactivar")
                                .hasAuthority(CATALOGO_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, CATALOGO).hasAuthority(CATALOGO_CREAR)
                        .requestMatchers(HttpMethod.PUT, CATALOGO).hasAuthority(CATALOGO_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, CATALOGO).hasAuthority(CATALOGO_ELIMINAR)

                        .requestMatchers(HttpMethod.GET, "/api/v1/depositos/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/depositos/*/activar", "/api/v1/depositos/*/desactivar")
                                .hasAuthority(DEPOSITO_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/depositos").hasAuthority(DEPOSITO_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/depositos/*").hasAuthority(DEPOSITO_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/depositos/*").hasAuthority(DEPOSITO_ELIMINAR)

                        // Clientes, tipos de cliente y metodos de pago: caja y ventas los consultan, escribir exige permiso
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**", "/api/v1/tipos-cliente/**",
                                "/api/v1/metodos-pago/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/clientes/*/activar", "/api/v1/clientes/*/desactivar",
                                "/api/v1/tipos-cliente/*/activar", "/api/v1/tipos-cliente/*/desactivar")
                                .hasAuthority(CLIENTE_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, CLIENTES).hasAuthority(CLIENTE_CREAR)
                        .requestMatchers(HttpMethod.PUT, CLIENTES).hasAuthority(CLIENTE_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, CLIENTES).hasAuthority(CLIENTE_ELIMINAR)

                        .requestMatchers(HttpMethod.PATCH, "/api/v1/metodos-pago/*/activar", "/api/v1/metodos-pago/*/desactivar")
                                .hasAuthority(METODO_PAGO_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/metodos-pago").hasAuthority(METODO_PAGO_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/metodos-pago/*").hasAuthority(METODO_PAGO_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/metodos-pago/*").hasAuthority(METODO_PAGO_ELIMINAR)

                        // Proveedores: datos de contacto, solo con permiso (incluso para leer)
                        .requestMatchers(HttpMethod.GET, "/api/v1/proveedores/**").hasAuthority(PROVEEDOR_VER)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/proveedores/*/activar", "/api/v1/proveedores/*/desactivar")
                                .hasAuthority(PROVEEDOR_ACTIVAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/proveedores").hasAuthority(PROVEEDOR_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/proveedores/*").hasAuthority(PROVEEDOR_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/proveedores/*").hasAuthority(PROVEEDOR_ELIMINAR)

                        // Pedidos a proveedores y recepciones: las acciones especiales van antes que el CRUD
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos/*/enviar").hasAuthority(PEDIDO_ENVIAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos/*/cancelar").hasAuthority(PEDIDO_CANCELAR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos/*/recepciones").hasAuthority(PEDIDO_RECIBIR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos/**").hasAuthority(PEDIDO_VER)
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos").hasAuthority(PEDIDO_CREAR)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/pedidos/*").hasAuthority(PEDIDO_EDITAR)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/pedidos/*").hasAuthority(PEDIDO_ELIMINAR)

                        // Reportes: cada familia con su permiso (se agregan a medida que existen)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/ventas/**").hasAuthority(REPORTE_VENTAS)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/inventario/**").hasAuthority(REPORTE_INVENTARIO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/caja/**").hasAuthority(REPORTE_CAJA)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/utilidad/**").hasAuthority(REPORTE_UTILIDAD)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reportes/compras/**").hasAuthority(REPORTE_COMPRAS)

                        // Inventario: consultar con INVENTARIO_VER; registrar movimientos con INVENTARIO_MOVER
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventario/**")
                                .hasAnyAuthority(INVENTARIO_VER, INVENTARIO_MOVER)
                        .requestMatchers("/api/v1/inventario/**").hasAuthority(INVENTARIO_MOVER)

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

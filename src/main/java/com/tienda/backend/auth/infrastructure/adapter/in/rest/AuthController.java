package com.tienda.backend.auth.infrastructure.adapter.in.rest;

import com.tienda.backend.auth.application.model.LoginComando;
import com.tienda.backend.auth.application.port.in.CambiarPasswordUseCase;
import com.tienda.backend.auth.application.port.in.CerrarSesionUseCase;
import com.tienda.backend.auth.application.port.in.LoginUseCase;
import com.tienda.backend.auth.application.port.in.ObtenerPerfilUseCase;
import com.tienda.backend.auth.application.port.in.RefrescarSesionUseCase;
import com.tienda.backend.auth.application.port.in.RestablecerPasswordUseCase;
import com.tienda.backend.auth.application.port.in.SolicitarRestablecimientoUseCase;
import com.tienda.backend.auth.infrastructure.adapter.in.rest.dto.CambiarPasswordRequest;
import com.tienda.backend.auth.infrastructure.adapter.in.rest.dto.LoginRequest;
import com.tienda.backend.auth.infrastructure.adapter.in.rest.dto.PerfilResponse;
import com.tienda.backend.auth.infrastructure.adapter.in.rest.dto.RefreshRequest;
import com.tienda.backend.auth.infrastructure.adapter.in.rest.dto.RestablecerPasswordRequest;
import com.tienda.backend.auth.infrastructure.adapter.in.rest.dto.SolicitarRestablecimientoRequest;
import com.tienda.backend.auth.infrastructure.adapter.in.rest.dto.TokenResponse;
import com.tienda.backend.auth.infrastructure.security.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final int USER_AGENT_MAX = 255;

    private final LoginUseCase loginUseCase;
    private final RefrescarSesionUseCase refrescarSesionUseCase;
    private final CerrarSesionUseCase cerrarSesionUseCase;
    private final CambiarPasswordUseCase cambiarPasswordUseCase;
    private final ObtenerPerfilUseCase obtenerPerfilUseCase;
    private final SolicitarRestablecimientoUseCase solicitarRestablecimientoUseCase;
    private final RestablecerPasswordUseCase restablecerPasswordUseCase;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String userAgent = http.getHeader(HttpHeaders.USER_AGENT);
        if (userAgent != null && userAgent.length() > USER_AGENT_MAX) {
            userAgent = userAgent.substring(0, USER_AGENT_MAX);
        }
        LoginComando comando = new LoginComando(request.username(), request.password(), http.getRemoteAddr(), userAgent);
        return ResponseEntity.ok(TokenResponse.from(loginUseCase.login(comando)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(TokenResponse.from(refrescarSesionUseCase.refrescar(request.refreshToken())));
    }

    /** Cierra la sesión de ese refresh token. El access token vigente expira solo (máx. 15 min); el cliente debe descartarlo. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        cerrarSesionUseCase.cerrarSesion(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-todas")
    public ResponseEntity<Void> logoutTodas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        cerrarSesionUseCase.cerrarTodas(usuario.id());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                @Valid @RequestBody CambiarPasswordRequest request) {
        cambiarPasswordUseCase.cambiarPassword(usuario.id(), request.passwordActual(), request.passwordNueva());
        return ResponseEntity.noContent().build();
    }

    /**
     * "Olvidé mi contraseña" (público). Responde siempre 202 con el mismo mensaje, exista o no la cuenta, para no
     * revelar qué usuarios existen. 429 si esa IP pidió demasiados enlaces.
     */
    @PostMapping("/olvide-password")
    public ResponseEntity<Map<String, String>> olvidePassword(@Valid @RequestBody SolicitarRestablecimientoRequest request,
                                                              HttpServletRequest http) {
        solicitarRestablecimientoUseCase.solicitar(request.identificador(), http.getRemoteAddr());
        return ResponseEntity.accepted().body(Map.of("mensaje",
                "Si la cuenta existe y está activa, recibirá un correo con las instrucciones para restablecer su contraseña."));
    }

    /** Fija la contraseña nueva con el token del enlace (público). Cierra todas las sesiones del usuario. */
    @PostMapping("/restablecer-password")
    public ResponseEntity<Void> restablecerPassword(@Valid @RequestBody RestablecerPasswordRequest request) {
        restablecerPasswordUseCase.restablecer(request.token(), request.passwordNueva());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<PerfilResponse> me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(PerfilResponse.from(obtenerPerfilUseCase.obtenerPerfil(usuario.id())));
    }
}

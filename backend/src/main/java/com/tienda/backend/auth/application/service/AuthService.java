package com.tienda.backend.auth.application.service;

import com.tienda.backend.auth.application.model.CredencialesUsuario;
import com.tienda.backend.auth.application.model.DatosAcceso;
import com.tienda.backend.auth.application.model.LoginComando;
import com.tienda.backend.auth.application.model.OrigenCambioPassword;
import com.tienda.backend.auth.application.model.ParTokens;
import com.tienda.backend.auth.application.port.in.CambiarPasswordUseCase;
import com.tienda.backend.auth.application.port.in.CerrarSesionUseCase;
import com.tienda.backend.auth.application.port.in.LoginUseCase;
import com.tienda.backend.auth.application.port.in.ObtenerPerfilUseCase;
import com.tienda.backend.auth.application.port.in.RefrescarSesionUseCase;
import com.tienda.backend.auth.application.port.in.RevocarAccesoUseCase;
import com.tienda.backend.auth.application.port.out.NotificadorCambioPasswordPort;
import com.tienda.backend.auth.application.port.out.PasswordVerifierPort;
import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.application.port.out.SesionRepositoryPort;
import com.tienda.backend.auth.application.port.out.TokenPort;
import com.tienda.backend.auth.application.port.out.UsuarioAutenticacionPort;
import com.tienda.backend.auth.domain.exception.CambioPasswordInvalidoException;
import com.tienda.backend.auth.domain.exception.CredencialesInvalidasException;
import com.tienda.backend.auth.domain.exception.RefreshTokenInvalidoException;
import com.tienda.backend.auth.domain.model.PoliticaPassword;
import com.tienda.backend.auth.domain.model.Sesion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements LoginUseCase, RefrescarSesionUseCase, CerrarSesionUseCase,
        CambiarPasswordUseCase, ObtenerPerfilUseCase, RevocarAccesoUseCase {

    private final UsuarioAutenticacionPort usuarioAutenticacionPort;
    private final SesionRepositoryPort sesionRepositoryPort;
    private final TokenPort tokenPort;
    private final RevocacionTokensPort revocacionTokensPort;
    private final PasswordVerifierPort passwordVerifierPort;
    private final NotificadorCambioPasswordPort notificadorCambioPasswordPort;

    /** Hash de relleno: se compara cuando el usuario no existe para que el login tarde igual y no revele si existe. */
    private final String hashFicticio;

    public AuthService(UsuarioAutenticacionPort usuarioAutenticacionPort,
                       SesionRepositoryPort sesionRepositoryPort,
                       TokenPort tokenPort,
                       RevocacionTokensPort revocacionTokensPort,
                       PasswordVerifierPort passwordVerifierPort,
                       NotificadorCambioPasswordPort notificadorCambioPasswordPort) {
        this.usuarioAutenticacionPort = usuarioAutenticacionPort;
        this.sesionRepositoryPort = sesionRepositoryPort;
        this.tokenPort = tokenPort;
        this.revocacionTokensPort = revocacionTokensPort;
        this.passwordVerifierPort = passwordVerifierPort;
        this.notificadorCambioPasswordPort = notificadorCambioPasswordPort;
        this.hashFicticio = passwordVerifierPort.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public ParTokens login(LoginComando comando) {
        String username = comando.username() == null ? "" : comando.username().trim().toLowerCase(Locale.ROOT);
        String password = comando.password() == null ? "" : comando.password();

        Optional<CredencialesUsuario> credenciales = usuarioAutenticacionPort.buscarCredencialesPorUsername(username);
        String hash = credenciales.map(CredencialesUsuario::passwordHash).orElse(hashFicticio);
        boolean passwordCorrecta = passwordVerifierPort.matches(password, hash);

        if (credenciales.isEmpty() || !passwordCorrecta || !credenciales.get().activo()) {
            throw new CredencialesInvalidasException();
        }

        DatosAcceso datos = usuarioAutenticacionPort.cargarDatosAcceso(credenciales.get().usuarioId())
                .orElseThrow(CredencialesInvalidasException::new);

        String refreshToken = tokenPort.generarRefreshToken();
        Sesion sesion = Sesion.nueva(
                datos.usuarioId(),
                tokenPort.hashRefreshToken(refreshToken),
                LocalDateTime.now().plusDays(tokenPort.diasDeVidaRefresh()),
                comando.ip(),
                comando.userAgent());
        sesionRepositoryPort.save(sesion);

        return new ParTokens(tokenPort.emitirAcceso(datos), refreshToken,
                tokenPort.segundosDeVidaAcceso(), datos.passwordTemporal());
    }

    /** Si el usuario ya no está activo la sesión se revoca y se persiste, por eso no se revierte la transacción. */
    @Override
    @Transactional(noRollbackFor = RefreshTokenInvalidoException.class)
    public ParTokens refrescar(String refreshToken) {
        LocalDateTime ahora = LocalDateTime.now();
        Sesion sesion = sesionRepositoryPort
                .findByRefreshTokenHash(tokenPort.hashRefreshToken(refreshToken == null ? "" : refreshToken))
                .orElseThrow(RefreshTokenInvalidoException::new);

        if (!sesion.estaVigente(ahora)) {
            throw new RefreshTokenInvalidoException();
        }

        Optional<DatosAcceso> datos = usuarioAutenticacionPort.cargarDatosAcceso(sesion.getUsuarioId())
                .filter(DatosAcceso::activo);
        if (datos.isEmpty()) {
            sesion.revocar(ahora);
            sesionRepositoryPort.save(sesion);
            throw new RefreshTokenInvalidoException();
        }

        String nuevoRefresh = tokenPort.generarRefreshToken();
        sesion.rotar(tokenPort.hashRefreshToken(nuevoRefresh), ahora.plusDays(tokenPort.diasDeVidaRefresh()));
        sesionRepositoryPort.save(sesion);

        return new ParTokens(tokenPort.emitirAcceso(datos.get()), nuevoRefresh,
                tokenPort.segundosDeVidaAcceso(), datos.get().passwordTemporal());
    }

    @Override
    @Transactional
    public void cerrarSesion(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        sesionRepositoryPort.findByRefreshTokenHash(tokenPort.hashRefreshToken(refreshToken))
                .ifPresent(sesion -> {
                    sesion.revocar(LocalDateTime.now());
                    sesionRepositoryPort.save(sesion);
                });
    }

    @Override
    @Transactional
    public void cerrarTodas(Long usuarioId) {
        sesionRepositoryPort.revocarTodasDeUsuario(usuarioId, LocalDateTime.now());
        revocacionTokensPort.revocar(usuarioId);
    }

    @Override
    @Transactional
    public void cambiarPassword(Long usuarioId, String passwordActual, String passwordNueva) {
        CredencialesUsuario credenciales = usuarioAutenticacionPort.buscarCredencialesPorId(usuarioId)
                .orElseThrow(CredencialesInvalidasException::new);

        if (passwordActual == null || !passwordVerifierPort.matches(passwordActual, credenciales.passwordHash())) {
            throw new CambioPasswordInvalidoException("La contraseña actual es incorrecta");
        }
        PoliticaPassword.validar(passwordNueva);
        if (passwordVerifierPort.matches(passwordNueva, credenciales.passwordHash())) {
            throw new CambioPasswordInvalidoException("La nueva contraseña debe ser distinta de la actual");
        }

        usuarioAutenticacionPort.guardarPassword(usuarioId, passwordVerifierPort.encode(passwordNueva));
        cerrarTodas(usuarioId);
        usuarioAutenticacionPort.buscarContactoPorId(usuarioId)
                .ifPresent(c -> notificadorCambioPasswordPort.avisarCambio(c, OrigenCambioPassword.CAMBIO_PROPIO));
    }

    @Override
    @Transactional(readOnly = true)
    public DatosAcceso obtenerPerfil(Long usuarioId) {
        return usuarioAutenticacionPort.cargarDatosAcceso(usuarioId)
                .orElseThrow(CredencialesInvalidasException::new);
    }

    /** Se ejecuta tras confirmarse el cambio que originó el evento, así que necesita su propia transacción. */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revocarAccesoDeUsuario(Long usuarioId) {
        revocarAcceso(usuarioId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revocarAccesoDeRol(Long rolId) {
        List<Long> usuarioIds = usuarioAutenticacionPort.usuarioIdsPorRol(rolId);
        usuarioIds.forEach(this::revocarAcceso);
    }

    private void revocarAcceso(Long usuarioId) {
        sesionRepositoryPort.revocarTodasDeUsuario(usuarioId, LocalDateTime.now());
        revocacionTokensPort.revocar(usuarioId);
    }
}

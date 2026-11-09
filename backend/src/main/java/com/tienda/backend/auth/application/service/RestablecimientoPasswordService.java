package com.tienda.backend.auth.application.service;

import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.model.CredencialesUsuario;
import com.tienda.backend.auth.application.model.OrigenCambioPassword;
import com.tienda.backend.auth.application.port.in.RestablecerPasswordUseCase;
import com.tienda.backend.auth.application.port.in.SolicitarRestablecimientoUseCase;
import com.tienda.backend.auth.application.port.out.LimitadorSolicitudesPort;
import com.tienda.backend.auth.application.port.out.NotificadorCambioPasswordPort;
import com.tienda.backend.auth.application.port.out.NotificadorRestablecimientoPort;
import com.tienda.backend.auth.application.port.out.PasswordVerifierPort;
import com.tienda.backend.auth.application.port.out.RestablecimientoRepositoryPort;
import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.application.port.out.SesionRepositoryPort;
import com.tienda.backend.auth.application.port.out.TokenPort;
import com.tienda.backend.auth.application.port.out.UsuarioAutenticacionPort;
import com.tienda.backend.auth.domain.exception.TokenRestablecimientoInvalidoException;
import com.tienda.backend.auth.domain.model.PoliticaPassword;
import com.tienda.backend.auth.domain.model.TokenRestablecimiento;
import com.tienda.backend.shared.domain.exception.DemasiadasSolicitudesException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * "Olvidé mi contraseña" con un enlace de un solo uso enviado por correo. Decisiones de seguridad:
 * el pedido nunca revela si la cuenta existe (responde igual y el envío es asíncrono); el token es aleatorio, se guarda
 * solo su hash, vence a los 30 minutos y se gasta al usarlo; pedir uno nuevo invalida los anteriores; hay un límite por
 * IP y otro por cuenta; y al restablecer se cierran todas las sesiones del usuario.
 */
@Service
@RequiredArgsConstructor
public class RestablecimientoPasswordService implements SolicitarRestablecimientoUseCase, RestablecerPasswordUseCase {
    static final int MINUTOS_VALIDEZ = 30;
    /** Enlaces que se le generan a una misma cuenta en una hora; los pedidos de más se ignoran sin avisar. */
    static final int MAX_SOLICITUDES_POR_CUENTA_POR_HORA = 3;

    private final UsuarioAutenticacionPort usuarioAutenticacionPort;
    private final RestablecimientoRepositoryPort restablecimientoRepositoryPort;
    private final SesionRepositoryPort sesionRepositoryPort;
    private final RevocacionTokensPort revocacionTokensPort;
    private final TokenPort tokenPort;
    private final PasswordVerifierPort passwordVerifierPort;
    private final LimitadorSolicitudesPort limitadorSolicitudesPort;
    private final NotificadorRestablecimientoPort notificadorRestablecimientoPort;
    private final NotificadorCambioPasswordPort notificadorCambioPasswordPort;

    @Override
    @Transactional
    public void solicitar(String identificador, String ip) {
        // El límite por IP sí se informa (429): no depende de que la cuenta exista, así que no revela nada
        if (!limitadorSolicitudesPort.permitir("ip:" + (ip == null ? "desconocida" : ip))) {
            throw new DemasiadasSolicitudesException("Demasiadas solicitudes. Intente de nuevo en unos minutos.");
        }

        String buscado = identificador == null ? "" : identificador.trim().toLowerCase(Locale.ROOT);
        if (buscado.isEmpty()) {
            return;
        }
        Optional<ContactoUsuario> contacto = usuarioAutenticacionPort.buscarContactoPorIdentificador(buscado)
                .filter(ContactoUsuario::activo);
        if (contacto.isEmpty()) {
            return;   // misma respuesta que si existiera
        }

        Long usuarioId = contacto.get().usuarioId();
        LocalDateTime ahora = LocalDateTime.now();
        if (restablecimientoRepositoryPort.contarSolicitudesDesde(usuarioId, ahora.minusHours(1))
                >= MAX_SOLICITUDES_POR_CUENTA_POR_HORA) {
            return;
        }

        restablecimientoRepositoryPort.invalidarPendientes(usuarioId, ahora);
        String token = tokenPort.generarTokenOpaco();
        restablecimientoRepositoryPort.save(TokenRestablecimiento.nuevo(usuarioId, tokenPort.hashTokenOpaco(token),
                ahora.plusMinutes(MINUTOS_VALIDEZ), ip));
        notificadorRestablecimientoPort.enviarEnlace(contacto.get(), token, MINUTOS_VALIDEZ);
    }

    @Override
    @Transactional
    public void restablecer(String token, String passwordNueva) {
        PoliticaPassword.validar(passwordNueva);
        LocalDateTime ahora = LocalDateTime.now();

        TokenRestablecimiento enlace = restablecimientoRepositoryPort
                .findByTokenHash(tokenPort.hashTokenOpaco(token == null ? "" : token))
                .filter(t -> t.estaVigente(ahora))
                .orElseThrow(TokenRestablecimientoInvalidoException::new);

        Long usuarioId = enlace.getUsuarioId();
        usuarioAutenticacionPort.buscarCredencialesPorId(usuarioId)
                .filter(CredencialesUsuario::activo)
                .orElseThrow(TokenRestablecimientoInvalidoException::new);

        // Si dos peticiones usan el mismo enlace a la vez, solo una lo gasta
        if (!restablecimientoRepositoryPort.marcarUsado(enlace.getId(), ahora)) {
            throw new TokenRestablecimientoInvalidoException();
        }

        usuarioAutenticacionPort.guardarPassword(usuarioId, passwordVerifierPort.encode(passwordNueva));
        restablecimientoRepositoryPort.invalidarPendientes(usuarioId, ahora);
        // La contraseña anterior deja de valer: nadie que la tuviera conserva la sesión
        sesionRepositoryPort.revocarTodasDeUsuario(usuarioId, ahora);
        revocacionTokensPort.revocar(usuarioId);

        // Aviso al dueño de la cuenta: si no fue él quien lo pidió, es la señal para que reaccione
        usuarioAutenticacionPort.buscarContactoPorId(usuarioId)
                .ifPresent(c -> notificadorCambioPasswordPort.avisarCambio(c, OrigenCambioPassword.RESTABLECIMIENTO));
    }
}

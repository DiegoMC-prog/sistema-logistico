package com.tienda.backend.auth.application.service;

import com.tienda.backend.auth.application.model.CredencialesUsuario;
import com.tienda.backend.auth.application.port.in.VerificarCredencialesUseCase;
import com.tienda.backend.auth.application.port.out.BloqueoCuentaPort;
import com.tienda.backend.auth.application.port.out.PasswordVerifierPort;
import com.tienda.backend.auth.application.port.out.UsuarioAutenticacionPort;
import com.tienda.backend.auth.domain.exception.CredencialesInvalidasException;
import com.tienda.backend.auth.domain.exception.CuentaBloqueadaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Verificación de credenciales con bloqueo por intentos fallidos (R06). Se acepta el nombre de usuario o el correo
 * (RF-01). Tras {@code app.auth.bloqueo.intentos} fallos seguidos la cuenta queda bloqueada
 * {@code app.auth.bloqueo.minutos}; un inicio correcto o un restablecimiento de contraseña la desbloquea.
 */
@Service
public class VerificadorCredenciales implements VerificarCredencialesUseCase {

    private final UsuarioAutenticacionPort usuarioAutenticacionPort;
    private final PasswordVerifierPort passwordVerifierPort;
    private final BloqueoCuentaPort bloqueoCuentaPort;
    private final int intentosMaximos;
    private final int minutosBloqueo;

    /** Hash de relleno: se compara cuando el usuario no existe para que la respuesta tarde igual y no revele si existe. */
    private final String hashFicticio;

    public VerificadorCredenciales(UsuarioAutenticacionPort usuarioAutenticacionPort,
                                   PasswordVerifierPort passwordVerifierPort,
                                   BloqueoCuentaPort bloqueoCuentaPort,
                                   @Value("${app.auth.bloqueo.intentos:5}") int intentosMaximos,
                                   @Value("${app.auth.bloqueo.minutos:15}") int minutosBloqueo) {
        this.usuarioAutenticacionPort = usuarioAutenticacionPort;
        this.passwordVerifierPort = passwordVerifierPort;
        this.bloqueoCuentaPort = bloqueoCuentaPort;
        this.intentosMaximos = intentosMaximos;
        this.minutosBloqueo = Math.max(minutosBloqueo, 1);
        this.hashFicticio = passwordVerifierPort.encode(UUID.randomUUID().toString());
    }

    @Override
    public Long verificar(String identificador, String password) {
        String id = identificador == null ? "" : identificador.trim().toLowerCase(Locale.ROOT);
        String clave = password == null ? "" : password;

        Optional<CredencialesUsuario> credenciales = usuarioAutenticacionPort.buscarCredencialesPorIdentificador(id);
        LocalDateTime ahora = LocalDateTime.now();
        if (credenciales.isPresent()) {
            Optional<LocalDateTime> hasta = bloqueoCuentaPort.bloqueadaHasta(credenciales.get().usuarioId())
                    .filter(ahora::isBefore);
            if (hasta.isPresent()) {
                // Ni siquiera se compara la contraseña: durante el bloqueo no se puede seguir probando
                throw new CuentaBloqueadaException(minutosRestantes(ahora, hasta.get()));
            }
        }

        String hash = credenciales.map(CredencialesUsuario::passwordHash).orElse(hashFicticio);
        boolean correcta = passwordVerifierPort.matches(clave, hash);

        if (credenciales.isEmpty()) {
            throw new CredencialesInvalidasException();
        }
        Long usuarioId = credenciales.get().usuarioId();
        if (!correcta) {
            if (intentosMaximos > 0) {
                LocalDateTime hasta = ahora.plusMinutes(minutosBloqueo);
                if (bloqueoCuentaPort.registrarFallo(usuarioId, intentosMaximos, hasta)) {
                    throw new CuentaBloqueadaException(minutosBloqueo);
                }
            }
            throw new CredencialesInvalidasException();
        }
        if (!credenciales.get().activo()) {
            throw new CredencialesInvalidasException();
        }
        bloqueoCuentaPort.reiniciar(usuarioId);
        return usuarioId;
    }

    private static long minutosRestantes(LocalDateTime ahora, LocalDateTime hasta) {
        long segundos = Duration.between(ahora, hasta).getSeconds();
        return (segundos + 59) / 60;
    }
}

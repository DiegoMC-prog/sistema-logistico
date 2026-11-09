package com.tienda.backend.auth.application.service;

import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.model.CredencialesUsuario;
import com.tienda.backend.auth.application.model.OrigenCambioPassword;
import com.tienda.backend.auth.application.port.out.NotificadorCambioPasswordPort;
import com.tienda.backend.auth.application.port.out.PasswordVerifierPort;
import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.application.port.out.SesionRepositoryPort;
import com.tienda.backend.auth.application.port.out.TokenPort;
import com.tienda.backend.auth.application.port.out.UsuarioAutenticacionPort;
import com.tienda.backend.auth.domain.exception.CambioPasswordInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Cambio de contraseña desde la cuenta: avisa por correo, y solo cuando el cambio realmente se hizo. */
class AuthServiceCambioPasswordTest {
    private static final Long USUARIO = 3L;
    private static final ContactoUsuario CONTACTO = new ContactoUsuario(USUARIO, "ana", "Ana", "ana@mail.com", true);

    private UsuarioAutenticacionPort usuarios;
    private PasswordVerifierPort passwords;
    private NotificadorCambioPasswordPort aviso;
    private AuthService servicio;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioAutenticacionPort.class);
        passwords = mock(PasswordVerifierPort.class);
        aviso = mock(NotificadorCambioPasswordPort.class);
        servicio = new AuthService(usuarios, mock(SesionRepositoryPort.class), mock(TokenPort.class),
                mock(RevocacionTokensPort.class), passwords, aviso);

        when(usuarios.buscarCredencialesPorId(USUARIO)).thenReturn(Optional.of(new CredencialesUsuario(USUARIO, "hashActual", true)));
        when(usuarios.buscarContactoPorId(USUARIO)).thenReturn(Optional.of(CONTACTO));
        when(passwords.matches("ClaveActual#1", "hashActual")).thenReturn(true);
        when(passwords.encode(anyString())).thenReturn("hashNuevo");
    }

    @Test
    void alCambiarLaPasswordSeAvisaAlDuenioDeLaCuenta() {
        servicio.cambiarPassword(USUARIO, "ClaveActual#1", "ClaveNueva#2");

        verify(usuarios).guardarPassword(USUARIO, "hashNuevo");
        verify(aviso).avisarCambio(CONTACTO, OrigenCambioPassword.CAMBIO_PROPIO);
    }

    @Test
    void conLaPasswordActualIncorrectaNoSeCambiaNiSeAvisa() {
        assertThrows(CambioPasswordInvalidoException.class, () -> servicio.cambiarPassword(USUARIO, "incorrecta", "ClaveNueva#2"));

        verifyNoInteractions(aviso);
    }

    @Test
    void conUnaPasswordNuevaInvalidaNoSeAvisa() {
        assertThrows(CambioPasswordInvalidoException.class, () -> servicio.cambiarPassword(USUARIO, "ClaveActual#1", "corta"));

        verifyNoInteractions(aviso);
    }
}

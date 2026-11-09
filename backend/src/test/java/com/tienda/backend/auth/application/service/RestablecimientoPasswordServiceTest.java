package com.tienda.backend.auth.application.service;

import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.model.CredencialesUsuario;
import com.tienda.backend.auth.application.model.OrigenCambioPassword;
import com.tienda.backend.auth.application.port.out.LimitadorSolicitudesPort;
import com.tienda.backend.auth.application.port.out.NotificadorCambioPasswordPort;
import com.tienda.backend.auth.application.port.out.NotificadorRestablecimientoPort;
import com.tienda.backend.auth.application.port.out.PasswordVerifierPort;
import com.tienda.backend.auth.application.port.out.RestablecimientoRepositoryPort;
import com.tienda.backend.auth.application.port.out.RevocacionTokensPort;
import com.tienda.backend.auth.application.port.out.SesionRepositoryPort;
import com.tienda.backend.auth.application.port.out.TokenPort;
import com.tienda.backend.auth.application.port.out.UsuarioAutenticacionPort;
import com.tienda.backend.auth.domain.exception.CambioPasswordInvalidoException;
import com.tienda.backend.auth.domain.exception.TokenRestablecimientoInvalidoException;
import com.tienda.backend.auth.domain.model.TokenRestablecimiento;
import com.tienda.backend.shared.domain.exception.DemasiadasSolicitudesException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RestablecimientoPasswordServiceTest {
    private static final Long USUARIO = 7L;

    private UsuarioAutenticacionPort usuarios;
    private RestablecimientoRepositoryPort tokens;
    private SesionRepositoryPort sesiones;
    private RevocacionTokensPort revocacion;
    private TokenPort tokenPort;
    private PasswordVerifierPort passwords;
    private LimitadorSolicitudesPort limitador;
    private NotificadorRestablecimientoPort notificador;
    private NotificadorCambioPasswordPort avisoCambio;
    private RestablecimientoPasswordService servicio;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioAutenticacionPort.class);
        tokens = mock(RestablecimientoRepositoryPort.class);
        sesiones = mock(SesionRepositoryPort.class);
        revocacion = mock(RevocacionTokensPort.class);
        tokenPort = mock(TokenPort.class);
        passwords = mock(PasswordVerifierPort.class);
        limitador = mock(LimitadorSolicitudesPort.class);
        notificador = mock(NotificadorRestablecimientoPort.class);
        avisoCambio = mock(NotificadorCambioPasswordPort.class);
        servicio = new RestablecimientoPasswordService(usuarios, tokens, sesiones, revocacion, tokenPort, passwords,
                limitador, notificador, avisoCambio);

        when(limitador.permitir(anyString())).thenReturn(true);
        when(tokenPort.generarTokenOpaco()).thenReturn("TOKEN-EN-CLARO");
        when(tokenPort.hashTokenOpaco(anyString())).thenAnswer(i -> "hash(" + i.getArgument(0) + ")");
        when(passwords.encode(anyString())).thenAnswer(i -> "bcrypt(" + i.getArgument(0) + ")");
        when(usuarios.buscarContactoPorId(USUARIO)).thenReturn(Optional.of(contacto(true)));
    }

    private static ContactoUsuario contacto(boolean activo) {
        return new ContactoUsuario(USUARIO, "ana", "Ana", "ana@mail.com", activo);
    }

    // ------------------------------------------------------------------ solicitar

    @Test
    void solicitarGeneraUnEnlaceGuardaSoloElHashYAvisaPorCorreo() {
        when(usuarios.buscarContactoPorIdentificador("ana")).thenReturn(Optional.of(contacto(true)));

        servicio.solicitar("  Ana ", "10.0.0.1");

        verify(tokens).invalidarPendientes(eq(USUARIO), any());
        ArgumentCaptor<TokenRestablecimiento> guardado = ArgumentCaptor.forClass(TokenRestablecimiento.class);
        verify(tokens).save(guardado.capture());
        assertEquals("hash(TOKEN-EN-CLARO)", guardado.getValue().getTokenHash());   // nunca el token en claro
        assertEquals(USUARIO, guardado.getValue().getUsuarioId());
        assertTrue(guardado.getValue().estaVigente(LocalDateTime.now().plusMinutes(29)));
        assertFalse(guardado.getValue().estaVigente(LocalDateTime.now().plusMinutes(31)));   // vence a los 30 minutos
        verify(notificador).enviarEnlace(contacto(true), "TOKEN-EN-CLARO", 30);
    }

    @Test
    void siLaCuentaNoExisteNoSeHaceNadaYNoSeLanzaError() {
        when(usuarios.buscarContactoPorIdentificador(anyString())).thenReturn(Optional.empty());

        servicio.solicitar("nadie", "10.0.0.1");

        verify(tokens, never()).save(any());
        verifyNoInteractions(notificador);
    }

    @Test
    void unaCuentaInactivaTampocoRecibeEnlace() {
        when(usuarios.buscarContactoPorIdentificador("ana")).thenReturn(Optional.of(contacto(false)));

        servicio.solicitar("ana", "10.0.0.1");

        verify(tokens, never()).save(any());
        verifyNoInteractions(notificador);
    }

    @Test
    void unIdentificadorVacioSeIgnoraSinConsultarNada() {
        servicio.solicitar("   ", "10.0.0.1");
        servicio.solicitar(null, "10.0.0.1");

        verifyNoInteractions(usuarios, tokens, notificador);
    }

    @Test
    void superadoElLimiteDeLaCuentaSeIgnoraElPedidoEnSilencio() {
        when(usuarios.buscarContactoPorIdentificador("ana")).thenReturn(Optional.of(contacto(true)));
        when(tokens.contarSolicitudesDesde(eq(USUARIO), any())).thenReturn((long) RestablecimientoPasswordService.MAX_SOLICITUDES_POR_CUENTA_POR_HORA);

        servicio.solicitar("ana", "10.0.0.1");   // no lanza: no se le debe revelar al atacante que existe

        verify(tokens, never()).save(any());
        verifyNoInteractions(notificador);
    }

    @Test
    void superadoElLimiteDeLaIpSeResponde429SinConsultarLaCuenta() {
        when(limitador.permitir("ip:10.0.0.1")).thenReturn(false);

        assertThrows(DemasiadasSolicitudesException.class, () -> servicio.solicitar("ana", "10.0.0.1"));

        verifyNoInteractions(usuarios, tokens, notificador);
    }

    // ------------------------------------------------------------------ restablecer

    private TokenRestablecimiento enlace(LocalDateTime expira, LocalDateTime usado) {
        return new TokenRestablecimiento(55L, USUARIO, "hash(TOKEN)", expira, usado, null);
    }

    private void conEnlaceVigente() {
        when(tokens.findByTokenHash("hash(TOKEN)")).thenReturn(Optional.of(enlace(LocalDateTime.now().plusMinutes(10), null)));
        when(usuarios.buscarCredencialesPorId(USUARIO)).thenReturn(Optional.of(new CredencialesUsuario(USUARIO, "hashViejo", true)));
        when(tokens.marcarUsado(eq(55L), any())).thenReturn(true);
    }

    @Test
    void restablecerCambiaLaPasswordGastaElEnlaceYCierraTodasLasSesiones() {
        conEnlaceVigente();

        servicio.restablecer("TOKEN", "NuevaClave#1");

        verify(usuarios).guardarPassword(USUARIO, "bcrypt(NuevaClave#1)");
        verify(tokens).marcarUsado(eq(55L), any());
        verify(tokens).invalidarPendientes(eq(USUARIO), any());
        verify(sesiones).revocarTodasDeUsuario(eq(USUARIO), any());
        verify(revocacion).revocar(USUARIO);
        verify(avisoCambio).avisarCambio(contacto(true), OrigenCambioPassword.RESTABLECIMIENTO);
    }

    @Test
    void unEnlaceInexistenteVencidoOUsadoNoSirve() {
        when(tokens.findByTokenHash("hash(NO-EXISTE)")).thenReturn(Optional.empty());
        assertThrows(TokenRestablecimientoInvalidoException.class, () -> servicio.restablecer("NO-EXISTE", "NuevaClave#1"));

        when(tokens.findByTokenHash("hash(VENCIDO)"))
                .thenReturn(Optional.of(enlace(LocalDateTime.now().minusMinutes(1), null)));
        assertThrows(TokenRestablecimientoInvalidoException.class, () -> servicio.restablecer("VENCIDO", "NuevaClave#1"));

        when(tokens.findByTokenHash("hash(USADO)"))
                .thenReturn(Optional.of(enlace(LocalDateTime.now().plusMinutes(10), LocalDateTime.now().minusMinutes(2))));
        assertThrows(TokenRestablecimientoInvalidoException.class, () -> servicio.restablecer("USADO", "NuevaClave#1"));

        // en ninguno de los tres casos se tocó la contraseña ni las sesiones
        verify(usuarios, never()).guardarPassword(anyLong(), anyString());
        verifyNoInteractions(sesiones, revocacion, avisoCambio);
    }

    @Test
    void unEnlaceDeUnaCuentaQueYaNoEstaActivaNoSirve() {
        when(tokens.findByTokenHash("hash(TOKEN)")).thenReturn(Optional.of(enlace(LocalDateTime.now().plusMinutes(10), null)));
        when(usuarios.buscarCredencialesPorId(USUARIO)).thenReturn(Optional.of(new CredencialesUsuario(USUARIO, "hashViejo", false)));

        assertThrows(TokenRestablecimientoInvalidoException.class, () -> servicio.restablecer("TOKEN", "NuevaClave#1"));

        verify(usuarios, never()).guardarPassword(anyLong(), anyString());
    }

    @Test
    void siOtraPeticionGastoElEnlaceAntesNoSeCambiaLaPassword() {
        conEnlaceVigente();
        when(tokens.marcarUsado(eq(55L), any())).thenReturn(false);   // dos usos simultaneos: este llego segundo

        assertThrows(TokenRestablecimientoInvalidoException.class, () -> servicio.restablecer("TOKEN", "NuevaClave#1"));

        verify(usuarios, never()).guardarPassword(anyLong(), anyString());
        verifyNoInteractions(sesiones, revocacion, avisoCambio);
    }

    @Test
    void unaPasswordQueNoCumpleLasReglasSeRechazaSinGastarElEnlace() {
        assertThrows(CambioPasswordInvalidoException.class, () -> servicio.restablecer("TOKEN", "corta"));
        assertThrows(CambioPasswordInvalidoException.class, () -> servicio.restablecer("TOKEN", null));
        assertThrows(CambioPasswordInvalidoException.class, () -> servicio.restablecer("TOKEN", "x".repeat(80)));

        verifyNoInteractions(tokens, usuarios);
    }
}

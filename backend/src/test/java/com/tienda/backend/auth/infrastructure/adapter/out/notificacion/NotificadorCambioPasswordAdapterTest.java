package com.tienda.backend.auth.infrastructure.adapter.out.notificacion;

import com.tienda.backend.auth.application.mail.CorreoPasswordCambiada;
import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.auth.application.model.OrigenCambioPassword;
import com.tienda.backend.shared.application.model.CorreoMensaje;
import com.tienda.backend.shared.application.port.out.EnviadorCorreoPort;
import com.tienda.backend.shared.domain.exception.CorreoNoEnviadoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificadorCambioPasswordAdapterTest {
    private static final ContactoUsuario DESTINATARIO = new ContactoUsuario(7L, "ana", "Ana", "ana@mail.com", true);

    @Test
    void enviaElAvisoAlCorreoDelUsuarioConSuOrigen() {
        EnviadorCorreoPort enviador = mock(EnviadorCorreoPort.class);

        new NotificadorCambioPasswordAdapter(enviador).avisarCambio(DESTINATARIO, OrigenCambioPassword.RESTABLECIMIENTO);

        ArgumentCaptor<CorreoMensaje> correo = ArgumentCaptor.forClass(CorreoMensaje.class);
        verify(enviador).enviar(correo.capture());
        assertEquals("ana@mail.com", correo.getValue().para());
        assertEquals("Su contraseña fue cambiada", correo.getValue().asunto());
        assertEquals("RESTABLECIMIENTO", correo.getValue().variables().get("origen"));
        assertEquals("ana", correo.getValue().variables().get("username"));
    }

    @Test
    void siFallaElEnvioNoSePropagaPorqueElCambioDeContrasenaYaOcurrio() {
        EnviadorCorreoPort enviador = mock(EnviadorCorreoPort.class);
        doThrow(new CorreoNoEnviadoException("ana@mail.com")).when(enviador).enviar(any());

        assertDoesNotThrow(() -> new NotificadorCambioPasswordAdapter(enviador)
                .avisarCambio(DESTINATARIO, OrigenCambioPassword.CAMBIO_PROPIO));
    }

    // ------------------------------------------------------------------ plantilla

    private String renderizar(String nombre, OrigenCambioPassword origen) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine motor = new SpringTemplateEngine();
        motor.setTemplateResolver(resolver);

        CorreoMensaje correo = CorreoPasswordCambiada.crear("ana@mail.com", nombre, "ana",
                LocalDateTime.of(2026, 11, 8, 14, 5), origen);
        Context contexto = new Context(Locale.of("es"));
        contexto.setVariables(correo.variables());
        contexto.setVariable("asunto", correo.asunto());
        contexto.setVariable("tienda", "Mi Tienda");
        contexto.setVariable("colorPrincipal", "#1d4ed8");
        return motor.process(correo.plantilla(), contexto);
    }

    @Test
    void laPlantillaIndicaCuandoYComoSeCambioYQueHacerSiNoFueElUsuario() {
        String html = renderizar("Ana", OrigenCambioPassword.RESTABLECIMIENTO);

        assertTrue(html.contains("08/11/2026 14:05"), "fecha y hora");
        assertTrue(html.contains("usando el enlace de recuperación"));
        assertTrue(html.contains("¿No fue usted?"));
        assertTrue(html.contains("Mi Tienda"));
    }

    @Test
    void elTextoCambiaSegunComoSeCambioLaContrasena() {
        String propio = renderizar("Ana", OrigenCambioPassword.CAMBIO_PROPIO);

        assertTrue(propio.contains("desde su cuenta"));
        assertFalse(propio.contains("enlace de recuperación"));
    }

    @Test
    void elCorreoDeSeguridadNoLlevaEnlacesParaNoParecerPhishing() {
        assertFalse(renderizar("Ana", OrigenCambioPassword.RESTABLECIMIENTO).contains("href"));
    }

    @Test
    void laPlantillaEscapaElHtmlDelNombre() {
        String html = renderizar("<script>alert(1)</script>", OrigenCambioPassword.CAMBIO_PROPIO);

        assertFalse(html.contains("<script>alert(1)</script>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }
}

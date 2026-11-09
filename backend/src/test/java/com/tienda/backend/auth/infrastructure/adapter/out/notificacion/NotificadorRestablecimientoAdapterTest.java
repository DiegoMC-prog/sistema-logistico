package com.tienda.backend.auth.infrastructure.adapter.out.notificacion;

import com.tienda.backend.auth.application.mail.CorreoRestablecerPassword;
import com.tienda.backend.auth.application.model.ContactoUsuario;
import com.tienda.backend.shared.application.model.CorreoMensaje;
import com.tienda.backend.shared.application.port.out.EnviadorCorreoPort;
import com.tienda.backend.shared.domain.exception.CorreoNoEnviadoException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificadorRestablecimientoAdapterTest {

    private static final ContactoUsuario DESTINATARIO = new ContactoUsuario(7L, "ana", "Ana", "ana@mail.com", true);

    private NotificadorRestablecimientoAdapter adaptador(EnviadorCorreoPort enviador, String url) {
        NotificadorRestablecimientoAdapter adaptador = new NotificadorRestablecimientoAdapter(enviador);
        ReflectionTestUtils.setField(adaptador, "urlRestablecer", url);
        return adaptador;
    }

    @Test
    void elEnlaceUsaLaUrlConfiguradaYAgregaElToken() {
        EnviadorCorreoPort enviador = mock(EnviadorCorreoPort.class);

        adaptador(enviador, "http://localhost:4200/restablecer").enviarEnlace(DESTINATARIO, "abc-123_XYZ", 30);

        ArgumentCaptor<CorreoMensaje> correo = ArgumentCaptor.forClass(CorreoMensaje.class);
        verify(enviador).enviar(correo.capture());
        assertEquals("ana@mail.com", correo.getValue().para());
        assertEquals("http://localhost:4200/restablecer?token=abc-123_XYZ", correo.getValue().variables().get("enlace"));
        assertEquals(30, correo.getValue().variables().get("minutos"));
    }

    @Test
    void siLaUrlYaTieneParametrosElTokenSeAgregaConAmpersand() {
        assertEquals("https://app.tienda.com/reset?origen=correo&token=T",
                adaptador(mock(EnviadorCorreoPort.class), "https://app.tienda.com/reset?origen=correo").enlace("T"));
    }

    @Test
    void siFallaElEnvioNoSePropagaElErrorPorqueQuienPidioNoDebeEnterarse() {
        EnviadorCorreoPort enviador = mock(EnviadorCorreoPort.class);
        doThrow(new CorreoNoEnviadoException("ana@mail.com")).when(enviador).enviar(any());

        assertDoesNotThrow(() -> adaptador(enviador, "http://localhost:4200/restablecer").enviarEnlace(DESTINATARIO, "T", 30));
    }

    // ------------------------------------------------------------------ plantilla

    private String renderizar(String nombre) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine motor = new SpringTemplateEngine();
        motor.setTemplateResolver(resolver);

        CorreoMensaje correo = CorreoRestablecerPassword.crear("ana@mail.com", nombre,
                "http://localhost:4200/restablecer?token=T0KEN", 30);
        Context contexto = new Context(Locale.of("es"));
        contexto.setVariables(correo.variables());
        contexto.setVariable("asunto", correo.asunto());
        contexto.setVariable("tienda", "Mi Tienda");
        contexto.setVariable("colorPrincipal", "#1d4ed8");
        return motor.process(correo.plantilla(), contexto);
    }

    @Test
    void laPlantillaMuestraElBotonConElEnlaceLaVigenciaYElNombreDeLaTienda() {
        String html = renderizar("Ana");

        assertTrue(html.contains("href=\"http://localhost:4200/restablecer?token=T0KEN\""), "boton con el enlace");
        assertTrue(html.contains("30</span> minutos"), "vigencia");
        assertTrue(html.contains("Hola <strong>Ana</strong>"));
        assertTrue(html.contains("Mi Tienda"), "cabecera de la tienda");
        assertTrue(html.contains("#1d4ed8"));
    }

    @Test
    void laPlantillaEscapaElHtmlDelNombre() {
        String html = renderizar("<script>alert(1)</script>");

        assertFalse(html.contains("<script>alert(1)</script>"));
        assertTrue(html.contains("&lt;script&gt;"));
    }
}

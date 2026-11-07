package com.tienda.backend.shared.infrastructure.adapter.out.mail;

import com.tienda.backend.shared.application.model.CorreoMensaje;
import com.tienda.backend.shared.application.port.out.EnviadorCorreoPort;
import com.tienda.backend.shared.domain.exception.CorreoNoEnviadoException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;

/**
 * Único punto del sistema que habla con el servidor de correo. Renderiza la plantilla de Thymeleaf
 * (con el nombre de la tienda y el color principal disponibles como variables globales) y envía el HTML.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnviadorCorreoAdapter implements EnviadorCorreoPort {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String remitente;

    @Value("${app.mail.tienda-nombre}")
    private String tiendaNombre;

    @Value("${app.mail.color-principal}")
    private String colorPrincipal;

    @Override
    public void enviar(CorreoMensaje mensaje) {
        String html = renderizar(mensaje);

        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(mensaje.para());
            helper.setSubject(mensaje.asunto());
            helper.setText(html, true);
            mailSender.send(mime);
        } catch (MailException | MessagingException e) {
            // No se registra el contenido del correo: puede incluir contraseñas temporales.
            log.warn("No se pudo enviar el correo '{}' a {}: {}", mensaje.plantilla(), mensaje.para(), e.getMessage());
            throw new CorreoNoEnviadoException(mensaje.para());
        }
    }

    private String renderizar(CorreoMensaje mensaje) {
        Context contexto = new Context(Locale.of("es"));
        if (mensaje.variables() != null) {
            contexto.setVariables(mensaje.variables());
        }
        contexto.setVariable("asunto", mensaje.asunto());
        contexto.setVariable("tienda", tiendaNombre);
        contexto.setVariable("colorPrincipal", colorPrincipal);
        return templateEngine.process(mensaje.plantilla(), contexto);
    }
}

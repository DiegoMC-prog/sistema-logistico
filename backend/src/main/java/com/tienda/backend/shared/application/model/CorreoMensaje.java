package com.tienda.backend.shared.application.model;

import java.util.Map;

/**
 * Un correo a enviar. No lleva el HTML ya armado: indica qué plantilla usar
 * (archivo en {@code resources/templates}, sin extensión) y los datos que se insertan en ella.
 */
public record CorreoMensaje(String para, String asunto, String plantilla, Map<String, Object> variables) {
}

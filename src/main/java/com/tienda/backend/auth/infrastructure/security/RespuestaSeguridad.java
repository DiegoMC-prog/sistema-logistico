package com.tienda.backend.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Escribe errores de seguridad con el mismo formato ProblemDetail del resto de la API. Los fallos del filtro ocurren
 * antes de llegar a Spring MVC, así que {@code GlobalExceptionHandler} no los ve. Los textos son fijos (sin datos del
 * usuario), por eso se arma el JSON a mano sin riesgo de inyección.
 */
public final class RespuestaSeguridad {

    private RespuestaSeguridad() {
    }

    public static void escribir(HttpServletResponse response, int status, String titulo, String detalle) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + titulo
                + "\",\"status\":" + status + ",\"detail\":\"" + detalle + "\"}");
    }
}

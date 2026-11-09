package com.tienda.backend.auth.application.model;

public record LoginComando(String username, String password, String ip, String userAgent) {
}

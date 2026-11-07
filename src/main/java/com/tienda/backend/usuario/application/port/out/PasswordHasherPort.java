package com.tienda.backend.usuario.application.port.out;

public interface PasswordHasherPort {
    String encode(String passwordEnClaro);
}

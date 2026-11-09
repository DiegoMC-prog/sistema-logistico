package com.tienda.backend.auth.application.port.out;

public interface PasswordVerifierPort {
    boolean matches(String passwordEnClaro, String passwordHash);

    String encode(String passwordEnClaro);
}

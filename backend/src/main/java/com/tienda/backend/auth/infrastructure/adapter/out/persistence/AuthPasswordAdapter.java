package com.tienda.backend.auth.infrastructure.adapter.out.persistence;

import com.tienda.backend.auth.application.port.out.PasswordVerifierPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthPasswordAdapter implements PasswordVerifierPort {
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String passwordEnClaro, String passwordHash) {
        return passwordEncoder.matches(passwordEnClaro, passwordHash);
    }

    @Override
    public String encode(String passwordEnClaro) {
        return passwordEncoder.encode(passwordEnClaro);
    }
}

package com.tienda.backend.usuario.infrastructure.adapter.out.persistence;

import com.tienda.backend.usuario.application.port.out.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordHasherPort {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String passwordEnClaro) {
        return passwordEncoder.encode(passwordEnClaro);
    }
}

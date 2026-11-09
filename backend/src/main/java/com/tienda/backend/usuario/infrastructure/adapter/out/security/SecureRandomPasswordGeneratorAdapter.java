package com.tienda.backend.usuario.infrastructure.adapter.out.security;

import com.tienda.backend.usuario.application.port.out.PasswordGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Genera contraseñas de 12 caracteres con mayúscula, minúscula, número y símbolo, sin caracteres ambiguos. */
@Component
public class SecureRandomPasswordGeneratorAdapter implements PasswordGeneratorPort {
    private static final int LONGITUD = 12;
    private static final String MINUSCULAS = "abcdefghijkmnopqrstuvwxyz";
    private static final String MAYUSCULAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String NUMEROS = "23456789";
    private static final String SIMBOLOS = "!@#$%*-_";
    private static final String TODOS = MINUSCULAS + MAYUSCULAS + NUMEROS + SIMBOLOS;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generar() {
        List<Character> caracteres = new ArrayList<>(LONGITUD);
        caracteres.add(elegir(MINUSCULAS));
        caracteres.add(elegir(MAYUSCULAS));
        caracteres.add(elegir(NUMEROS));
        caracteres.add(elegir(SIMBOLOS));
        while (caracteres.size() < LONGITUD) {
            caracteres.add(elegir(TODOS));
        }
        Collections.shuffle(caracteres, random);

        StringBuilder password = new StringBuilder(LONGITUD);
        caracteres.forEach(password::append);
        return password.toString();
    }

    private char elegir(String conjunto) {
        return conjunto.charAt(random.nextInt(conjunto.length()));
    }
}

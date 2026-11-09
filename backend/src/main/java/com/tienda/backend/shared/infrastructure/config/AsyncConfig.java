package com.tienda.backend.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Habilita {@code @Async}: tareas que no deben hacer esperar la respuesta (p. ej. enviar un correo). */
@Configuration
@EnableAsync
public class AsyncConfig {
}

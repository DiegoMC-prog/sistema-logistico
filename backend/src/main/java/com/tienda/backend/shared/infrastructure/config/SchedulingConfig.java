package com.tienda.backend.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita {@code @Scheduled}: tareas periódicas como la generación diaria de alertas. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

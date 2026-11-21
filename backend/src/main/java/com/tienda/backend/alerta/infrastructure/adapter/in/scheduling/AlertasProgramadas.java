package com.tienda.backend.alerta.infrastructure.adapter.in.scheduling;

import com.tienda.backend.alerta.application.model.ResultadoGeneracion;
import com.tienda.backend.alerta.application.port.in.GenerarAlertasUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara el generador de alertas todos los días (por defecto a las 06:00, {@code app.alertas.cron}) y, si
 * {@code app.alertas.al-iniciar} está activo, también al arrancar la aplicación para no esperar al día siguiente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertasProgramadas {

    private final GenerarAlertasUseCase generarAlertasUseCase;

    @Value("${app.alertas.al-iniciar:true}")
    private boolean alIniciar;

    @Scheduled(cron = "${app.alertas.cron:0 0 6 * * *}")
    public void diaria() {
        ejecutar("programada");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        if (alIniciar) {
            ejecutar("al iniciar");
        }
    }

    private void ejecutar(String origen) {
        try {
            ResultadoGeneracion r = generarAlertasUseCase.generar();
            log.info("Alertas ({}): {} por vencer, {} vencidos, {} stock bajo, {} resueltas",
                    origen, r.vencimiento(), r.vencido(), r.stockBajo(), r.resueltas());
        } catch (RuntimeException ex) {
            // Una falla de la tarea no debe tumbar el arranque ni el planificador
            log.error("No se pudieron generar las alertas ({})", origen, ex);
        }
    }
}

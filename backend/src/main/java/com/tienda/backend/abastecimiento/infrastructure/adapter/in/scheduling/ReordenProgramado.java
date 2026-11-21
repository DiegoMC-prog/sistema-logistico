package com.tienda.backend.abastecimiento.infrastructure.adapter.in.scheduling;

import com.tienda.backend.abastecimiento.application.model.CalculoResumen;
import com.tienda.backend.abastecimiento.application.model.OrigenCalculo;
import com.tienda.backend.abastecimiento.application.port.in.RecalcularSugerenciasUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Recalcula las sugerencias cada noche (por defecto 02:30, {@code app.reorden.cron}) con las ventas y recepciones del
 * día. Al arrancar, si nunca se calculó, ejecuta un primer cálculo para que la pantalla no quede vacía.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReordenProgramado {

    private final RecalcularSugerenciasUseCase recalcularUseCase;

    @Scheduled(cron = "${app.reorden.cron:0 30 2 * * *}")
    public void nocturno() {
        ejecutar();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alArrancar() {
        try {
            if (!recalcularUseCase.existeCalculo()) {
                ejecutar();
            }
        } catch (RuntimeException ex) {
            log.error("No se pudo verificar el cálculo inicial de reorden", ex);
        }
    }

    private void ejecutar() {
        try {
            CalculoResumen c = recalcularUseCase.recalcular(OrigenCalculo.PROGRAMADO);
            log.info("Reorden: cálculo {} con {} productos evaluados, {} a reordenar", c.id(), c.productosEvaluados(),
                    c.productosAReordenar());
        } catch (RuntimeException ex) {
            log.error("Falló el recálculo programado de sugerencias de reorden", ex);
        }
    }
}

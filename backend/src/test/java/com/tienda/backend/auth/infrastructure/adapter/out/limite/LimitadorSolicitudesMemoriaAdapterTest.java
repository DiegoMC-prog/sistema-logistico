package com.tienda.backend.auth.infrastructure.adapter.out.limite;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimitadorSolicitudesMemoriaAdapterTest {

    private final LimitadorSolicitudesMemoriaAdapter limitador = new LimitadorSolicitudesMemoriaAdapter();
    private final Instant inicio = Instant.parse("2026-11-08T10:00:00Z");

    @Test
    void permiteHastaElMaximoYLuegoRechaza() {
        for (int i = 0; i < LimitadorSolicitudesMemoriaAdapter.MAXIMO; i++) {
            assertTrue(limitador.permitir("ip:1", inicio.plusSeconds(i)), "solicitud " + (i + 1));
        }
        assertFalse(limitador.permitir("ip:1", inicio.plusSeconds(10)));
        assertFalse(limitador.permitir("ip:1", inicio.plusSeconds(11)));   // seguir insistiendo no lo reabre
    }

    @Test
    void cadaClaveTieneSuPropioLimite() {
        for (int i = 0; i < LimitadorSolicitudesMemoriaAdapter.MAXIMO; i++) {
            limitador.permitir("ip:1", inicio);
        }
        assertFalse(limitador.permitir("ip:1", inicio));
        assertTrue(limitador.permitir("ip:2", inicio));
    }

    @Test
    void trasLaVentanaVuelvePermitir() {
        for (int i = 0; i < LimitadorSolicitudesMemoriaAdapter.MAXIMO; i++) {
            limitador.permitir("ip:1", inicio);
        }
        assertFalse(limitador.permitir("ip:1", inicio.plusSeconds(60)));
        assertTrue(limitador.permitir("ip:1", inicio.plus(LimitadorSolicitudesMemoriaAdapter.VENTANA).plusSeconds(1)));
    }
}

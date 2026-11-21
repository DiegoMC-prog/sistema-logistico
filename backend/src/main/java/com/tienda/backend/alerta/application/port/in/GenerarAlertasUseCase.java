package com.tienda.backend.alerta.application.port.in;

import com.tienda.backend.alerta.application.model.ResultadoGeneracion;

/** Revisa lotes y existencias, crea las alertas nuevas y resuelve las que dejaron de aplicar. Es idempotente. */
public interface GenerarAlertasUseCase {
    ResultadoGeneracion generar();
}

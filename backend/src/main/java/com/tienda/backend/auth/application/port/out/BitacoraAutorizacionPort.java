package com.tienda.backend.auth.application.port.out;

import com.tienda.backend.shared.application.model.AutorizacionOtorgada;

/** Tabla autorizaciones_operacion (migración V11): solo se inserta. */
public interface BitacoraAutorizacionPort {
    void registrar(AutorizacionOtorgada autorizacion, Long referenciaId);
}

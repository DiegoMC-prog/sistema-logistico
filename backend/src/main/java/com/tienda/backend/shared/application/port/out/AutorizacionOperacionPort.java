package com.tienda.backend.shared.application.port.out;

import com.tienda.backend.shared.application.model.AutorizacionOtorgada;
import com.tienda.backend.shared.application.model.CredencialesSupervisor;
import com.tienda.backend.shared.application.model.OperacionAutorizable;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * R08: devoluciones, cambios y anulaciones sobre el monto umbral exigen la autorización de un administrador.
 * Uso: {@code exigir} antes de modificar nada y {@code registrar} con el id del registro creado, en la misma transacción.
 */
public interface AutorizacionOperacionPort {

    /**
     * Vacío si el monto no supera el umbral. Si lo supera, la autoriza el propio usuario cuando tiene el permiso de
     * autorizar; si no, las credenciales de un administrador.
     *
     * @throws com.tienda.backend.shared.domain.exception.AutorizacionRequeridaException si hacen falta y no vinieron
     * @throws com.tienda.backend.shared.domain.exception.AutorizacionInvalidaException  si las enviadas no sirven
     */
    Optional<AutorizacionOtorgada> exigir(OperacionAutorizable operacion, BigDecimal monto,
                                          CredencialesSupervisor credenciales);

    /** Deja la autorización en la bitácora (no hace nada si no hizo falta). */
    void registrar(Optional<AutorizacionOtorgada> autorizacion, Long referenciaId);
}

package com.tienda.backend.caja.application.port.out;

import com.tienda.backend.caja.application.model.DepositoRef;

import java.util.Optional;

public interface CajaDepositoConsultaPort {
    Optional<DepositoRef> buscarPorId(Long id);
}

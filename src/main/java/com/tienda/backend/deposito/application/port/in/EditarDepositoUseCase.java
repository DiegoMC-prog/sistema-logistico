package com.tienda.backend.deposito.application.port.in;

import com.tienda.backend.deposito.domain.model.Deposito;

public interface EditarDepositoUseCase {
    Deposito editar(Long id, Deposito deposito);
}

package com.tienda.backend.deposito.application.port.in;

import com.tienda.backend.deposito.domain.model.Deposito;

public interface CrearDepositoUseCase {
    Deposito create(Deposito deposito);
}

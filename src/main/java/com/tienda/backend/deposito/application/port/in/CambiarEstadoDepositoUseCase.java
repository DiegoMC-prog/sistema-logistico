package com.tienda.backend.deposito.application.port.in;

import com.tienda.backend.deposito.domain.model.Deposito;

public interface CambiarEstadoDepositoUseCase {
    Deposito activar(Long id);
    Deposito desactivar(Long id);
}

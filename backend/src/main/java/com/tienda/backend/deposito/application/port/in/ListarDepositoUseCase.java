package com.tienda.backend.deposito.application.port.in;

import com.tienda.backend.deposito.application.model.DepositoFiltro;
import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.shared.application.model.PageResult;

public interface ListarDepositoUseCase {
    PageResult<Deposito> listar(DepositoFiltro filtro, int page, int size);
}

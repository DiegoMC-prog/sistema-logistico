package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.deposito.domain.model.Deposito;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DepositoConsultaPort {
    Optional<Deposito> obtenerPorId(Long depositoId);

    List<Deposito> obtenerPorIds(Collection<Long> depositoIds);
}

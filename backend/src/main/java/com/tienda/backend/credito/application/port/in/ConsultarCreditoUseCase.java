package com.tienda.backend.credito.application.port.in;

import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.DeudorResumen;
import com.tienda.backend.credito.application.model.EstadoCuentaCliente;

import java.util.List;

public interface ConsultarCreditoUseCase {
    EstadoCuentaCliente estadoCuenta(Long clienteId);

    AbonoResumen abono(Long abonoId);

    List<DeudorResumen> deudores();
}

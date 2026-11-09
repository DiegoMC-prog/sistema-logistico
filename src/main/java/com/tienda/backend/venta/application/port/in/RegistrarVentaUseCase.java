package com.tienda.backend.venta.application.port.in;

import com.tienda.backend.venta.application.model.RegistrarVentaComando;
import com.tienda.backend.venta.application.model.ResultadoRegistroVenta;

public interface RegistrarVentaUseCase {
    ResultadoRegistroVenta registrar(RegistrarVentaComando comando);
}

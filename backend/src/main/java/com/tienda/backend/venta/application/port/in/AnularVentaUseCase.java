package com.tienda.backend.venta.application.port.in;

import com.tienda.backend.shared.application.model.CredencialesSupervisor;
import com.tienda.backend.venta.application.model.VentaVista;

public interface AnularVentaUseCase {
    /** R08: sobre el monto umbral hacen falta las credenciales de un administrador (null si no se enviaron). */
    VentaVista anular(Long ventaId, String motivo, CredencialesSupervisor autorizacion);
}

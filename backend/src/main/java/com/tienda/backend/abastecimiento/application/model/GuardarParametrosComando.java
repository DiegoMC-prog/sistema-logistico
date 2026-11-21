package com.tienda.backend.abastecimiento.application.model;

import java.math.BigDecimal;

/** Un valor null vuelve al global. */
public record GuardarParametrosComando(BigDecimal costoPedido, BigDecimal costoAlmacenPct, BigDecimal nivelServicio,
                                       Long proveedorId, boolean activo) {
}

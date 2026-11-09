package com.tienda.backend.inventario.application.model;

import java.util.List;

public record ResultadoSalidaFefo(Long movimientoId, List<AsignacionFefo> asignaciones) {
}

package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.presentacion.domain.model.Presentacion;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface PresentacionConsultaPort {
    Optional<Presentacion> obtenerPorId(Long presentacionId);

    /** Abreviatura de la unidad base (kg, u...) de cada producto indicado. */
    Map<Long, String> abreviaturaUnidadBasePorProducto(Collection<Long> productoIds);
}

package com.tienda.backend.catalogo.application.port.out;

import com.tienda.backend.catalogo.application.model.PrecioCatalogo;

import java.util.Optional;

public interface PrecioCatalogoPort {
    /** Precio de venta de una unidad de la presentación; vacío si no tiene precio aplicable. */
    Optional<PrecioCatalogo> precioUnitario(Long presentacionId, Long tipoClienteId);
}

package com.tienda.backend.presentacion.infrastructure.adapter.in.rest.dto;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;
import com.tienda.backend.presentacion.domain.model.Presentacion;

import java.math.BigDecimal;

public record PresentacionResponse(
        Long id,
        Long productoId,
        UnidadMedidaResumen unidadMedida,
        String codigoBarras,
        BigDecimal factorConversion,
        boolean esUnidadBase
) {
    public record UnidadMedidaResumen(Long id, String nombre, String abreviatura) {
    }

    public static PresentacionResponse from(PresentacionDetalle detalle) {
        Presentacion p = detalle.presentacion();
        return new PresentacionResponse(
                p.getId(),
                p.getProductoId(),
                new UnidadMedidaResumen(p.getUnidadMedidaId(), detalle.unidadNombre(), detalle.unidadAbreviatura()),
                p.getCodigoBarras(),
                p.getFactorConversion(),
                p.isEsUnidadBase());
    }
}

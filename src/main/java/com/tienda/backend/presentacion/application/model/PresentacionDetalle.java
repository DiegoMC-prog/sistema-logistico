package com.tienda.backend.presentacion.application.model;

import com.tienda.backend.presentacion.domain.model.Presentacion;

/** Modelo de lectura: la presentación junto con el nombre y la abreviatura de su unidad de medida. */
public record PresentacionDetalle(Presentacion presentacion, String unidadNombre, String unidadAbreviatura) {
}

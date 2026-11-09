package com.tienda.backend.unidadmedida.domain.model;

import com.tienda.backend.unidadmedida.domain.exception.UnidadMedidaDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UnidadMedida {
    private static final int NOMBRE_MAX = 150;
    private static final int ABREVIATURA_MAX = 10;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private String abreviatura;

    public UnidadMedida(Long id, String nombre, String abreviatura) {
        this.id = id;
        setNombre(nombre);
        setAbreviatura(abreviatura);
    }

    public UnidadMedida(String nombre, String abreviatura) {
        this(null, nombre, abreviatura);
    }

    public void editar(String nombre, String abreviatura) {
        setNombre(nombre);
        setAbreviatura(abreviatura);
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new UnidadMedidaDatosInvalidosException("El nombre de la unidad de medida es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > NOMBRE_MAX) {
            throw new UnidadMedidaDatosInvalidosException(
                    "El nombre de la unidad de medida no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setAbreviatura(String abreviatura) {
        if (abreviatura == null || abreviatura.isBlank()) {
            throw new UnidadMedidaDatosInvalidosException("La abreviatura es obligatoria y no puede estar vacía");
        }
        String limpia = abreviatura.trim();
        if (limpia.length() > ABREVIATURA_MAX) {
            throw new UnidadMedidaDatosInvalidosException(
                    "La abreviatura no puede superar los " + ABREVIATURA_MAX + " caracteres");
        }
        this.abreviatura = limpia;
    }
}

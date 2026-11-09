package com.tienda.backend.producto.domain.model;

import com.tienda.backend.producto.domain.exception.ProductoDatosInvalidosException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {
    private static final int NOMBRE_MAX = 150;

    @EqualsAndHashCode.Include
    private final Long id;

    private String nombre;
    private String descripcion;
    private Long categoriaId;
    private BigDecimal stockMinimo;
    private boolean esPerecedero;
    private EstadoProducto estado;

    /** Reconstrucción desde persistencia (o creación con id conocido). */
    public Producto(Long id, String nombre, String descripcion, Long categoriaId,
                    BigDecimal stockMinimo, boolean esPerecedero, EstadoProducto estado) {
        this.id = id;
        setNombre(nombre);
        setStockMinimo(stockMinimo);
        setEstado(estado);
        this.descripcion = normalizarDescripcion(descripcion);
        setCategoriaId(categoriaId);
        this.esPerecedero = esPerecedero;
    }

    /** Producto nuevo: nace ACTIVO y sin id. */
    public Producto(String nombre, String descripcion, Long categoriaId,
                    BigDecimal stockMinimo, boolean esPerecedero) {
        this(null, nombre, descripcion, categoriaId, stockMinimo, esPerecedero, EstadoProducto.ACTIVO);
    }

    public void editar(String nombre, String descripcion, Long categoriaId,
                       BigDecimal stockMinimo, boolean esPerecedero) {
        setNombre(nombre);
        setStockMinimo(stockMinimo);
        this.descripcion = normalizarDescripcion(descripcion);
        setCategoriaId(categoriaId);
        this.esPerecedero = esPerecedero;
    }

    public void activar() {
        this.estado = EstadoProducto.ACTIVO;
    }

    public void desactivar() {
        this.estado = EstadoProducto.INACTIVO;
    }

    public boolean estaActivo() {
        return estado == EstadoProducto.ACTIVO;
    }

    private void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ProductoDatosInvalidosException("El nombre del producto es obligatorio y no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > NOMBRE_MAX) {
            throw new ProductoDatosInvalidosException(
                    "El nombre del producto no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        this.nombre = limpio;
    }

    private void setCategoriaId(Long categoriaId) {
        if (categoriaId == null) {
            throw new ProductoDatosInvalidosException("La categoría del producto es obligatoria");
        }
        this.categoriaId = categoriaId;
    }

    private void setStockMinimo(BigDecimal stockMinimo) {
        if (stockMinimo == null) {
            throw new ProductoDatosInvalidosException("El stock mínimo es obligatorio");
        }
        if (stockMinimo.signum() < 0) {
            throw new ProductoDatosInvalidosException("El stock mínimo no puede ser negativo");
        }
        this.stockMinimo = stockMinimo;
    }

    private void setEstado(EstadoProducto estado) {
        if (estado == null) {
            throw new ProductoDatosInvalidosException("El estado del producto es obligatorio");
        }
        this.estado = estado;
    }

    private String normalizarDescripcion(String descripcion) {
        return (descripcion == null || descripcion.isBlank()) ? null : descripcion.trim();
    }
}

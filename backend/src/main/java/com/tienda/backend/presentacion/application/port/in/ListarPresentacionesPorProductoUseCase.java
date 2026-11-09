package com.tienda.backend.presentacion.application.port.in;

import com.tienda.backend.presentacion.application.model.PresentacionDetalle;

import java.util.List;

public interface ListarPresentacionesPorProductoUseCase {
    List<PresentacionDetalle> listarPorProducto(Long productoId);
}

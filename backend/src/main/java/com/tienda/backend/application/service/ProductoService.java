package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.ProductoRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.ProductoResponse;

public interface ProductoService {
    ProductoResponse crear(ProductoRequest request);
    ProductoResponse obtenerPorId(Long id);
    PageResponse<ProductoResponse> listar(int page, int size, String busqueda, Long categoriaId, Boolean estado);
    ProductoResponse actualizar(Long id, ProductoRequest request);
    ProductoResponse cambiarEstado(Long id, boolean estado);
    void eliminar(Long id);
}

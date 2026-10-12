package com.tienda.backend.application.service;

import com.tienda.backend.application.dto.request.ProductoRequest;
import com.tienda.backend.application.dto.response.PageResponse;
import com.tienda.backend.application.dto.response.ProductoResponse;
import com.tienda.backend.application.dto.response.ProductoStatsResponse;

import org.springframework.web.multipart.MultipartFile;

public interface ProductoService {
    ProductoResponse crear(ProductoRequest request, MultipartFile imagen);
    ProductoResponse obtenerPorId(Long id);
    PageResponse<ProductoResponse> listar(int page, int size, String busqueda, Long categoriaId, Boolean estado);
    ProductoResponse actualizar(Long id, ProductoRequest request, MultipartFile imagen);
    ProductoResponse cambiarEstado(Long id, boolean estado);
    void eliminar(Long id);
    ProductoStatsResponse obtenerEstadisticas();
}

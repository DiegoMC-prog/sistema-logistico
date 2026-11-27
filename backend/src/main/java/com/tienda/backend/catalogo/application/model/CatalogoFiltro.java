package com.tienda.backend.catalogo.application.model;

/**
 * Filtros del catálogo.
 *
 * @param depositoId      depósito del que se informa el stock (null = no se informa)
 * @param soloConStock    solo productos con stock en el depósito (requiere depositoId)
 * @param incluirVencidos cuenta también el stock de lotes vencidos (para darlo de baja); al vender no se incluye
 * @param conPrecios      resuelve el precio de venta de cada presentación para una unidad
 * @param tipoClienteId   tipo de cliente para el precio (null = precio general)
 */
public record CatalogoFiltro(Long depositoId, Long categoriaId, String search, boolean soloConStock,
                             boolean incluirVencidos, boolean conPrecios, Long tipoClienteId) {
}

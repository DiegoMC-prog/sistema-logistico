package com.tienda.backend.catalogo.application.service;

import com.tienda.backend.catalogo.application.model.CatalogoFiltro;
import com.tienda.backend.catalogo.application.model.CatalogoPresentacion;
import com.tienda.backend.catalogo.application.model.CatalogoProducto;
import com.tienda.backend.catalogo.application.port.in.ConsultarCatalogoUseCase;
import com.tienda.backend.catalogo.application.port.out.CatalogoRepositoryPort;
import com.tienda.backend.catalogo.application.port.out.PrecioCatalogoPort;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Catálogo para elegir productos (punto de venta y movimientos de inventario).
 * <p>No es transaccional a propósito: «sin precio» se informa con una excepción en el módulo de precios, y capturarla
 * dentro de una transacción compartida la dejaría marcada para deshacerse.
 */
@Service
@RequiredArgsConstructor
public class CatalogoService implements ConsultarCatalogoUseCase {

    private final CatalogoRepositoryPort repositorio;
    private final PrecioCatalogoPort precios;

    @Override
    public PageResult<CatalogoProducto> consultar(CatalogoFiltro filtro, int page, int size) {
        PageResult<CatalogoProducto> pagina =
                repositorio.listar(filtro, PageResult.safePage(page), PageResult.safeSize(size));
        if (!filtro.conPrecios()) {
            return pagina;
        }
        return pagina.map(producto -> producto.conPresentaciones(producto.presentaciones().stream()
                .map(p -> conPrecio(p, filtro.tipoClienteId()))
                .toList()));
    }

    private CatalogoPresentacion conPrecio(CatalogoPresentacion presentacion, Long tipoClienteId) {
        return precios.precioUnitario(presentacion.id(), tipoClienteId)
                .map(presentacion::conPrecio)
                .orElse(presentacion);
    }
}

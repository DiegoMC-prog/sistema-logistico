package com.tienda.backend.catalogo.infrastructure.adapter.in.rest;

import com.tienda.backend.catalogo.application.model.CatalogoFiltro;
import com.tienda.backend.catalogo.application.model.CatalogoProducto;
import com.tienda.backend.catalogo.application.port.in.ConsultarCatalogoUseCase;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo de productos con presentaciones, stock por depósito y precio de venta. Solo lectura. */
@RestController
@RequestMapping("/api/v1/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final ConsultarCatalogoUseCase consultarCatalogo;

    @GetMapping
    public ResponseEntity<PageResult<CatalogoProducto>> consultar(
            @RequestParam(required = false) Long depositoId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean soloConStock,
            @RequestParam(defaultValue = "false") boolean incluirVencidos,
            @RequestParam(defaultValue = "false") boolean conPrecios,
            @RequestParam(required = false) Long tipoClienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(consultarCatalogo.consultar(new CatalogoFiltro(depositoId, categoriaId, search,
                soloConStock, incluirVencidos, conPrecios, tipoClienteId), page, size));
    }
}

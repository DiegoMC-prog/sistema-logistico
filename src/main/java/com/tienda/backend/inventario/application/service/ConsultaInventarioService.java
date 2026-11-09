package com.tienda.backend.inventario.application.service;

import com.tienda.backend.inventario.application.model.MovimientoDetallado;
import com.tienda.backend.inventario.application.model.MovimientoFiltro;
import com.tienda.backend.inventario.application.model.StockDetalle;
import com.tienda.backend.inventario.application.model.StockFiltro;
import com.tienda.backend.inventario.application.port.in.ConsultarStockUseCase;
import com.tienda.backend.inventario.application.port.in.ListarMovimientosUseCase;
import com.tienda.backend.inventario.application.port.in.ObtenerMovimientoUseCase;
import com.tienda.backend.inventario.application.port.out.InventarioRepositoryPort;
import com.tienda.backend.inventario.application.port.out.MovimientoRepositoryPort;
import com.tienda.backend.inventario.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.inventario.domain.exception.MovimientoNotFoundException;
import com.tienda.backend.inventario.domain.model.Movimiento;
import com.tienda.backend.shared.application.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Consultas de solo lectura: stock actual e historial de movimientos. */
@Service
@RequiredArgsConstructor
public class ConsultaInventarioService implements ConsultarStockUseCase, ListarMovimientosUseCase,
        ObtenerMovimientoUseCase {

    private final InventarioRepositoryPort inventarioRepositoryPort;
    private final MovimientoRepositoryPort movimientoRepositoryPort;
    private final PresentacionConsultaPort presentacionConsultaPort;
    private final MovimientoVistaFactory vistaFactory;

    @Override
    @Transactional(readOnly = true)
    public PageResult<StockDetalle> consultarStock(StockFiltro filtro, int page, int size) {
        PageResult<StockDetalle> pagina = inventarioRepositoryPort.findAll(
                filtro, PageResult.safePage(page), PageResult.safeSize(size));
        if (pagina.content().isEmpty()) {
            return pagina;
        }

        // La unidad base de todos los productos de la página, en una sola consulta.
        Set<Long> productoIds = pagina.content().stream().map(StockDetalle::productoId).collect(Collectors.toSet());
        Map<Long, String> unidades = presentacionConsultaPort.abreviaturaUnidadBasePorProducto(productoIds);
        return pagina.map(stock -> stock.conUnidadBase(unidades.get(stock.productoId())));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MovimientoDetallado> listar(MovimientoFiltro filtro, int page, int size) {
        PageResult<Movimiento> pagina = movimientoRepositoryPort.findAll(
                filtro, PageResult.safePage(page), PageResult.safeSize(size));
        List<MovimientoDetallado> detallados = vistaFactory.construir(pagina.content());
        return new PageResult<>(detallados, pagina.page(), pagina.size(), pagina.totalElements(), pagina.totalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoDetallado obtener(Long id) {
        return vistaFactory.construir(movimientoRepositoryPort.findById(id)
                .orElseThrow(() -> new MovimientoNotFoundException(id)));
    }
}

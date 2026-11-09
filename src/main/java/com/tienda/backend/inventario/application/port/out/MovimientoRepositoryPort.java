package com.tienda.backend.inventario.application.port.out;

import com.tienda.backend.inventario.application.model.MovimientoFiltro;
import com.tienda.backend.inventario.domain.model.Movimiento;
import com.tienda.backend.shared.application.model.PageResult;

import java.util.Optional;

public interface MovimientoRepositoryPort {
    Movimiento save(Movimiento movimiento);
    Optional<Movimiento> findById(Long id);
    /** Historial, del más reciente al más antiguo. Cada movimiento trae sus líneas. */
    PageResult<Movimiento> findAll(MovimientoFiltro filtro, int page, int size);
}

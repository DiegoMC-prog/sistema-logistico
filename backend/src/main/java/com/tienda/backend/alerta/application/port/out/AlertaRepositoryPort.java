package com.tienda.backend.alerta.application.port.out;

import com.tienda.backend.alerta.application.model.AlertaFiltro;
import com.tienda.backend.alerta.application.model.AlertaResumen;
import com.tienda.backend.alerta.application.model.ConteoAlertas;
import com.tienda.backend.shared.application.model.PageResult;

import java.time.LocalDate;
import java.util.Optional;

public interface AlertaRepositoryPort {
    PageResult<AlertaResumen> listar(AlertaFiltro filtro, int page, int size);

    ConteoAlertas conteoNoLeidas();

    Optional<AlertaResumen> buscar(Long id);

    /** true si la alerta pasó a leída (false si ya lo estaba). */
    boolean marcarLeida(Long id, String usuario);

    int marcarTodasLeidas(String usuario);

    /** Lotes con stock que vencen entre {@code hoy} y {@code hasta} (inclusive) y aún no tienen su alerta. */
    int crearAlertasPorVencer(LocalDate hoy, LocalDate hasta);

    /** Lotes vencidos antes de {@code hoy} que todavía tienen stock y aún no tienen su alerta. */
    int crearAlertasVencidos(LocalDate hoy);

    /** Productos activos con stock total por debajo del mínimo sin una alerta de stock bajo activa. */
    int crearAlertasStockBajo();

    /** Resuelve las alertas cuya condición ya no se cumple; devuelve cuántas resolvió. */
    int resolverObsoletas(LocalDate hoy);
}

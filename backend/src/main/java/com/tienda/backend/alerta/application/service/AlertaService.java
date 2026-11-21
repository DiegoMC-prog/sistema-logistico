package com.tienda.backend.alerta.application.service;

import com.tienda.backend.alerta.application.model.AlertaFiltro;
import com.tienda.backend.alerta.application.model.AlertaResumen;
import com.tienda.backend.alerta.application.model.ConteoAlertas;
import com.tienda.backend.alerta.application.model.ResultadoGeneracion;
import com.tienda.backend.alerta.application.port.in.ConsultarAlertasUseCase;
import com.tienda.backend.alerta.application.port.in.GenerarAlertasUseCase;
import com.tienda.backend.alerta.application.port.in.MarcarAlertaLeidaUseCase;
import com.tienda.backend.alerta.application.port.out.AlertaRepositoryPort;
import com.tienda.backend.alerta.domain.exception.AlertaNotFoundException;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AlertaService implements ConsultarAlertasUseCase, MarcarAlertaLeidaUseCase, GenerarAlertasUseCase {

    private final AlertaRepositoryPort repositorio;
    private final UsuarioActualPort usuarioActualPort;
    /** Días de anticipación con que se avisa el vencimiento de un lote. */
    private final int diasVencimiento;

    public AlertaService(AlertaRepositoryPort repositorio, UsuarioActualPort usuarioActualPort,
                         @Value("${app.alertas.vencimiento.dias:30}") int diasVencimiento) {
        this.repositorio = repositorio;
        this.usuarioActualPort = usuarioActualPort;
        this.diasVencimiento = Math.max(diasVencimiento, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AlertaResumen> listar(AlertaFiltro filtro, int page, int size) {
        return repositorio.listar(filtro, PageResult.safePage(page), PageResult.safeSize(size));
    }

    @Override
    @Transactional(readOnly = true)
    public ConteoAlertas conteo() {
        return repositorio.conteoNoLeidas();
    }

    @Override
    @Transactional
    public AlertaResumen marcarLeida(Long alertaId) {
        repositorio.buscar(alertaId).orElseThrow(() -> new AlertaNotFoundException(alertaId));
        repositorio.marcarLeida(alertaId, usuarioActualPort.nombre());
        return repositorio.buscar(alertaId).orElseThrow(() -> new AlertaNotFoundException(alertaId));
    }

    @Override
    @Transactional
    public int marcarTodasLeidas() {
        return repositorio.marcarTodasLeidas(usuarioActualPort.nombre());
    }

    /**
     * Primero resuelve lo obsoleto (lotes agotados, avisos de vencimiento superados por el vencimiento real, stock
     * repuesto) y luego crea lo nuevo. Los índices únicos de la tabla evitan duplicados aunque corra dos veces.
     */
    @Override
    @Transactional
    public ResultadoGeneracion generar() {
        LocalDate hoy = LocalDate.now();
        int vencidos = repositorio.crearAlertasVencidos(hoy);
        int resueltas = repositorio.resolverObsoletas(hoy);
        int porVencer = repositorio.crearAlertasPorVencer(hoy, hoy.plusDays(diasVencimiento));
        int stockBajo = repositorio.crearAlertasStockBajo();
        return new ResultadoGeneracion(porVencer, vencidos, stockBajo, resueltas);
    }
}

package com.tienda.backend.caja.application.service;

import com.tienda.backend.caja.application.model.CobroPorMetodo;
import com.tienda.backend.caja.application.model.TotalesMovimientos;
import com.tienda.backend.caja.application.model.TurnoDetalle;
import com.tienda.backend.caja.application.model.TurnoFiltro;
import com.tienda.backend.caja.application.model.TurnoResumen;
import com.tienda.backend.caja.application.port.in.AbrirTurnoUseCase;
import com.tienda.backend.caja.application.port.in.CerrarTurnoUseCase;
import com.tienda.backend.caja.application.port.in.ConsultarEfectivoTurnoUseCase;
import com.tienda.backend.caja.application.port.in.ConsultarTurnoUseCase;
import com.tienda.backend.caja.application.port.in.RegistrarMovimientoCajaUseCase;
import com.tienda.backend.caja.application.port.out.CajaRepositoryPort;
import com.tienda.backend.caja.application.port.out.MovimientoCajaRepositoryPort;
import com.tienda.backend.caja.application.port.out.ResumenCobrosTurnoPort;
import com.tienda.backend.caja.application.port.out.TurnoRepositoryPort;
import com.tienda.backend.caja.domain.exception.CajaInactivaException;
import com.tienda.backend.caja.domain.exception.CajaNotFoundException;
import com.tienda.backend.caja.domain.exception.CajaSinTurnoAbiertoException;
import com.tienda.backend.caja.domain.exception.SaldoCajaInsuficienteException;
import com.tienda.backend.caja.domain.exception.TurnoNotFoundException;
import com.tienda.backend.caja.domain.exception.TurnoYaAbiertoException;
import com.tienda.backend.caja.domain.exception.UsuarioConTurnoAbiertoException;
import com.tienda.backend.caja.domain.model.Caja;
import com.tienda.backend.caja.domain.model.MovimientoCaja;
import com.tienda.backend.caja.domain.model.TipoMovimientoCaja;
import com.tienda.backend.caja.domain.model.TurnoCaja;
import com.tienda.backend.metodopago.domain.model.MetodoPago;
import com.tienda.backend.shared.application.model.PageResult;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.AccesoDenegadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurnoService implements AbrirTurnoUseCase, CerrarTurnoUseCase, RegistrarMovimientoCajaUseCase,
        ConsultarTurnoUseCase, ConsultarEfectivoTurnoUseCase {

    /** Permiso de supervisión (debe coincidir con el de la tabla permisos): ver y operar turnos de otros usuarios. */
    private static final String PERMISO_SUPERVISION = "CAJA_SUPERVISAR";

    private final CajaRepositoryPort cajaRepositoryPort;
    private final TurnoRepositoryPort turnoRepositoryPort;
    private final MovimientoCajaRepositoryPort movimientoRepositoryPort;
    private final ResumenCobrosTurnoPort resumenCobrosTurnoPort;
    private final UsuarioActualPort usuarioActualPort;

    @Override
    @Transactional
    public TurnoResumen abrir(Long cajaId, BigDecimal montoApertura) {
        Long usuarioId = usuarioActualPort.id();
        Caja caja = buscarCaja(cajaId);
        if (!caja.estaActiva()) {
            throw new CajaInactivaException(caja.getNombre());
        }
        if (turnoRepositoryPort.existsAbiertoByCajaId(cajaId)) {
            throw new TurnoYaAbiertoException();
        }
        if (turnoRepositoryPort.existsAbiertoByUsuarioId(usuarioId)) {
            throw new UsuarioConTurnoAbiertoException();
        }
        // Si dos aperturas llegan a la vez, los índices únicos parciales rechazan la segunda (409)
        TurnoCaja turno = turnoRepositoryPort.save(TurnoCaja.abrir(cajaId, usuarioId, montoApertura));
        return resumen(turno, caja.getNombre());
    }

    @Override
    @Transactional
    public TurnoResumen cerrar(Long turnoId, BigDecimal montoDeclarado) {
        TurnoCaja turno = turnoRepositoryPort.findByIdParaActualizar(turnoId)
                .orElseThrow(() -> new TurnoNotFoundException(turnoId));
        turno.exigirAbierto();
        exigirDuenoOSupervisor(turno, "cerrar");

        BigDecimal esperado = efectivoEsperado(turno, totales(turnoId), cobros(turnoId), reembolsos(turnoId));
        turno.cerrar(montoDeclarado, esperado);
        TurnoCaja cerrado = turnoRepositoryPort.save(turno);
        return resumen(cerrado, buscarCaja(cerrado.getCajaId()).getNombre());
    }

    @Override
    @Transactional
    public MovimientoCaja registrar(Long turnoId, TipoMovimientoCaja tipo, BigDecimal monto, String motivo) {
        TurnoCaja turno = turnoRepositoryPort.findByIdParaActualizar(turnoId)
                .orElseThrow(() -> new TurnoNotFoundException(turnoId));
        turno.exigirAbierto();
        exigirDuenoOSupervisor(turno, "registrar movimientos en");

        MovimientoCaja movimiento = MovimientoCaja.registrar(turnoId, usuarioActualPort.id(), tipo, monto, motivo);
        if (tipo == TipoMovimientoCaja.EGRESO) {
            BigDecimal disponible = efectivoEsperado(turno, totales(turnoId), cobros(turnoId), reembolsos(turnoId));
            if (movimiento.getMonto().compareTo(disponible) > 0) {
                throw new SaldoCajaInsuficienteException(disponible);
            }
        }
        return movimientoRepositoryPort.save(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResumen obtener(Long turnoId) {
        TurnoCaja turno = turnoRepositoryPort.findById(turnoId).orElseThrow(() -> new TurnoNotFoundException(turnoId));
        exigirDuenoOSupervisor(turno, "ver");
        return resumen(turno, buscarCaja(turno.getCajaId()).getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResumen turnoActual(Long cajaId) {
        Caja caja = buscarCaja(cajaId);
        TurnoCaja turno = turnoRepositoryPort.findAbiertoByCajaId(cajaId)
                .orElseThrow(() -> new CajaSinTurnoAbiertoException(cajaId));
        exigirDuenoOSupervisor(turno, "ver");
        return resumen(turno, caja.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TurnoDetalle> listar(TurnoFiltro filtro, int page, int size) {
        // Quien no supervisa solo ve sus propios turnos, pida lo que pida
        TurnoFiltro efectivo = esSupervisor() ? filtro : new TurnoFiltro(filtro.cajaId(), usuarioActualPort.id(),
                filtro.estado(), filtro.desde(), filtro.hasta());
        PageResult<TurnoCaja> pagina = turnoRepositoryPort.findAll(efectivo, PageResult.safePage(page), PageResult.safeSize(size));

        Set<Long> cajaIds = pagina.content().stream().map(TurnoCaja::getCajaId).collect(Collectors.toSet());
        Map<Long, String> nombres = cajaIds.isEmpty() ? Map.of()
                : cajaRepositoryPort.findAllById(cajaIds).stream().collect(Collectors.toMap(Caja::getId, Caja::getNombre));
        return pagina.map(t -> new TurnoDetalle(t, nombres.get(t.getCajaId())));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MovimientoCaja> movimientos(Long turnoId, int page, int size) {
        TurnoCaja turno = turnoRepositoryPort.findById(turnoId).orElseThrow(() -> new TurnoNotFoundException(turnoId));
        exigirDuenoOSupervisor(turno, "ver");
        return movimientoRepositoryPort.findByTurnoId(turnoId, PageResult.safePage(page), PageResult.safeSize(size));
    }

    // ---------- helpers ----------

    private Caja buscarCaja(Long id) {
        return cajaRepositoryPort.findById(id).orElseThrow(() -> new CajaNotFoundException(id));
    }

    private boolean esSupervisor() {
        return usuarioActualPort.tienePermiso(PERMISO_SUPERVISION);
    }

    private void exigirDuenoOSupervisor(TurnoCaja turno, String accion) {
        if (!turno.esDe(usuarioActualPort.id()) && !esSupervisor()) {
            throw new AccesoDenegadoException("No puede " + accion + " un turno de caja que pertenece a otro usuario.");
        }
    }

    private TotalesMovimientos totales(Long turnoId) {
        return movimientoRepositoryPort.totalesPorTurno(turnoId);
    }

    private List<CobroPorMetodo> cobros(Long turnoId) {
        return resumenCobrosTurnoPort.cobrosPorMetodo(turnoId);
    }

    private BigDecimal efectivoCobrado(List<CobroPorMetodo> cobros) {
        return cobros.stream()
                .filter(c -> MetodoPago.NOMBRE_EFECTIVO.equals(c.metodoPagoNombre()))
                .map(CobroPorMetodo::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal reembolsos(Long turnoId) {
        return resumenCobrosTurnoPort.reembolsosEfectivo(turnoId);
    }

    /** apertura + ingresos - egresos + ventas cobradas en efectivo - reembolsos de devoluciones en efectivo. */
    private BigDecimal efectivoEsperado(TurnoCaja turno, TotalesMovimientos t, List<CobroPorMetodo> cobros,
                                        BigDecimal reembolsos) {
        return turno.getMontoApertura().add(t.ingresos()).subtract(t.egresos()).add(efectivoCobrado(cobros))
                .subtract(reembolsos);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal efectivoDisponible(Long turnoId) {
        TurnoCaja turno = turnoRepositoryPort.findById(turnoId).orElseThrow(() -> new TurnoNotFoundException(turnoId));
        return efectivoEsperado(turno, totales(turnoId), cobros(turnoId), reembolsos(turnoId));
    }

    private TurnoResumen resumen(TurnoCaja turno, String cajaNombre) {
        TotalesMovimientos t = totales(turno.getId());
        List<CobroPorMetodo> cobros = cobros(turno.getId());
        BigDecimal reembolsos = reembolsos(turno.getId());
        BigDecimal esperado = turno.estaAbierto() ? efectivoEsperado(turno, t, cobros, reembolsos)
                : turno.getMontoCierreEsperado();
        BigDecimal diferencia = turno.estaAbierto() ? null : turno.getMontoCierreDeclarado().subtract(esperado);
        return new TurnoResumen(turno, cajaNombre, t.ingresos(), t.egresos(), cobros, efectivoCobrado(cobros),
                reembolsos, esperado, diferencia);
    }
}

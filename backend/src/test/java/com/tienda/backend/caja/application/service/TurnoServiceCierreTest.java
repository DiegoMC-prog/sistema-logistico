package com.tienda.backend.caja.application.service;

import com.tienda.backend.caja.application.model.CobroPorMetodo;
import com.tienda.backend.caja.application.model.TotalesMovimientos;
import com.tienda.backend.caja.application.model.TurnoResumen;
import com.tienda.backend.caja.application.port.out.CajaRepositoryPort;
import com.tienda.backend.caja.application.port.out.MovimientoCajaRepositoryPort;
import com.tienda.backend.caja.application.port.out.ResumenCobrosTurnoPort;
import com.tienda.backend.caja.application.port.out.TurnoRepositoryPort;
import com.tienda.backend.caja.domain.exception.CajaDatosInvalidosException;
import com.tienda.backend.caja.domain.exception.TurnoCerradoException;
import com.tienda.backend.caja.domain.model.Caja;
import com.tienda.backend.caja.domain.model.EstadoCaja;
import com.tienda.backend.caja.domain.model.EstadoTurno;
import com.tienda.backend.caja.domain.model.TurnoCaja;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.shared.domain.exception.AccesoDenegadoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TurnoServiceCierreTest {

    private static final Long TURNO = 3L;
    private static final Long CAJA = 1L;
    private static final Long CAJERO = 7L;
    private static final Long OTRO_USUARIO = 8L;

    private TurnoRepositoryPort turnoRepo;
    private UsuarioActualPort usuario;
    private TurnoService service;

    @BeforeEach
    void preparar() {
        turnoRepo = mock(TurnoRepositoryPort.class);
        usuario = mock(UsuarioActualPort.class);
        CajaRepositoryPort cajaRepo = mock(CajaRepositoryPort.class);
        MovimientoCajaRepositoryPort movimientoRepo = mock(MovimientoCajaRepositoryPort.class);
        ResumenCobrosTurnoPort cobrosPort = mock(ResumenCobrosTurnoPort.class);

        when(cajaRepo.findById(CAJA)).thenReturn(Optional.of(new Caja(CAJA, "Caja 1", 1L, EstadoCaja.ACTIVO)));
        when(turnoRepo.save(any(TurnoCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        // Movimientos del turno: ingresos 50, egresos 30, ventas en efectivo 200, tarjeta 80, abonos 20, reembolsos 10
        when(movimientoRepo.totalesPorTurno(TURNO)).thenReturn(new TotalesMovimientos(d("50.00"), d("30.00")));
        when(cobrosPort.cobrosPorMetodo(TURNO)).thenReturn(List.of(
                new CobroPorMetodo(1L, "EFECTIVO", d("200.00")),
                new CobroPorMetodo(2L, "TARJETA", d("80.00"))));
        when(cobrosPort.abonosEfectivo(TURNO)).thenReturn(d("20.00"));
        when(cobrosPort.reembolsosEfectivo(TURNO)).thenReturn(d("10.00"));

        service = new TurnoService(cajaRepo, turnoRepo, movimientoRepo, cobrosPort, usuario);
    }

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    private void turnoDelCajero(EstadoTurno estado) {
        TurnoCaja turno = new TurnoCaja(TURNO, CAJA, CAJERO, LocalDateTime.now().minusHours(8), d("100.00"),
                null, null, null, estado);
        when(turnoRepo.findByIdParaActualizar(TURNO)).thenReturn(Optional.of(turno));
    }

    @Test
    void calculaElEfectivoEsperadoYLaDiferencia() {
        // Arrange
        turnoDelCajero(EstadoTurno.ABIERTO);
        when(usuario.id()).thenReturn(CAJERO);

        // Act
        TurnoResumen r = service.cerrar(TURNO, d("325.00"));

        // Assert: 100 + 50 - 30 + 200 (solo efectivo) + 20 - 10 = 330; declaró 325 -> faltan 5
        assertEquals(0, d("330.00").compareTo(r.efectivoEsperado()));
        assertEquals(0, d("-5.00").compareTo(r.diferencia()));
        assertEquals(EstadoTurno.CERRADO, r.turno().getEstado());
    }

    @Test
    void unTurnoYaCerradoNoSePuedeVolverACerrar() {
        // Arrange
        turnoDelCajero(EstadoTurno.CERRADO);
        when(usuario.id()).thenReturn(CAJERO);

        // Act + Assert
        assertThrows(TurnoCerradoException.class, () -> service.cerrar(TURNO, d("100.00")));
        verify(turnoRepo, never()).save(any());
    }

    @Test
    void otroUsuarioSinPermisoNoPuedeCerrarElTurno() {
        // Arrange
        turnoDelCajero(EstadoTurno.ABIERTO);
        when(usuario.id()).thenReturn(OTRO_USUARIO);
        when(usuario.tienePermiso("CAJA_SUPERVISAR")).thenReturn(false);

        // Act + Assert
        assertThrows(AccesoDenegadoException.class, () -> service.cerrar(TURNO, d("330.00")));
        verify(turnoRepo, never()).save(any());
    }

    @Test
    void unSupervisorPuedeCerrarElTurnoDeOtro() {
        // Arrange
        turnoDelCajero(EstadoTurno.ABIERTO);
        when(usuario.id()).thenReturn(OTRO_USUARIO);
        when(usuario.tienePermiso("CAJA_SUPERVISAR")).thenReturn(true);

        // Act
        TurnoResumen r = service.cerrar(TURNO, d("330.00"));

        // Assert: cuadra exacto
        assertEquals(EstadoTurno.CERRADO, r.turno().getEstado());
        assertEquals(0, r.diferencia().signum());
    }

    @Test
    void unMontoDeclaradoNegativoSeRechaza() {
        // Arrange
        turnoDelCajero(EstadoTurno.ABIERTO);
        when(usuario.id()).thenReturn(CAJERO);

        // Act + Assert
        assertThrows(CajaDatosInvalidosException.class, () -> service.cerrar(TURNO, d("-1.00")));
        verify(turnoRepo, never()).save(any());
    }
}

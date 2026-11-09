package com.tienda.backend.credito.application.service;

import com.tienda.backend.credito.application.model.AbonoResumen;
import com.tienda.backend.credito.application.model.ClienteCredito;
import com.tienda.backend.credito.application.model.CuentaRef;
import com.tienda.backend.credito.application.model.MetodoPagoAbono;
import com.tienda.backend.credito.application.model.RegistrarAbonoComando;
import com.tienda.backend.credito.application.port.out.CreditoRepositoryPort;
import com.tienda.backend.credito.domain.exception.AbonoExcedeDeudaException;
import com.tienda.backend.credito.domain.exception.CreditoNoHabilitadoException;
import com.tienda.backend.credito.domain.exception.CuentaConAbonosException;
import com.tienda.backend.credito.domain.exception.LimiteCreditoExcedidoException;
import com.tienda.backend.credito.domain.exception.SinTurnoParaAbonoException;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditoServiceTest {

    private CreditoRepositoryPort repo;
    private UsuarioActualPort usuario;
    private CreditoService service;

    @BeforeEach
    void preparar() {
        repo = mock(CreditoRepositoryPort.class);
        usuario = mock(UsuarioActualPort.class);
        when(usuario.id()).thenReturn(7L);
        when(usuario.nombre()).thenReturn("cajero");
        service = new CreditoService(repo, usuario);
    }

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    private void cliente(boolean habilitado, String limite, String deuda) {
        when(repo.bloquearCliente(1L)).thenReturn(Optional.of(new ClienteCredito(1L, "Ana", true, habilitado, d(limite))));
        when(repo.deudaPendiente(1L)).thenReturn(d(deuda));
    }

    @Test
    void ventaACreditoExigeCreditoHabilitado() {
        cliente(false, "0", "0");
        assertThrows(CreditoNoHabilitadoException.class, () -> service.crearParaVenta(1L, 10L, d("50")));
        verify(repo, never()).insertarCuenta(anyLong(), anyLong(), any(), any());
    }

    @Test
    void ventaACreditoNoPuedePasarElLimite() {
        cliente(true, "100", "70");
        assertThrows(LimiteCreditoExcedidoException.class, () -> service.crearParaVenta(1L, 10L, d("40")));
    }

    @Test
    void ventaACreditoDentroDelLimiteCreaLaCuenta() {
        cliente(true, "100", "70");
        service.crearParaVenta(1L, 10L, d("30"));
        verify(repo).insertarCuenta(1L, 10L, d("30.00"), "cajero");
    }

    @Test
    void anularConAbonosSeRechaza() {
        when(repo.bloquearCuentaDeVenta(10L)).thenReturn(Optional.of(new CuentaRef(5L, 1L, d("50"), d("20"), "PENDIENTE")));
        when(repo.cuentaTieneAbonos(5L)).thenReturn(true);
        assertThrows(CuentaConAbonosException.class, () -> service.anularDeVenta(10L));
    }

    @Test
    void devolucionDescuentaDelSaldoHastaDondeAlcance() {
        when(repo.bloquearCuentaDeVenta(10L)).thenReturn(Optional.of(new CuentaRef(5L, 1L, d("50"), d("20"), "PENDIENTE")));
        BigDecimal aplicado = service.descontarPorDevolucion(10L, d("35"));
        assertEquals(d("20.00"), aplicado);
        verify(repo).actualizarCuenta(5L, d("0.00"), "PAGADA", "cajero");
    }

    private void preparaAbono(String deuda1, String deuda2) {
        when(repo.turnoAbiertoDeUsuario(7L)).thenReturn(Optional.of(3L));
        when(repo.buscarMetodoPago(1L)).thenReturn(Optional.of(new MetodoPagoAbono(1L, "EFECTIVO", true)));
        cliente(true, "500", "0");
        when(repo.cuentasPendientes(1L)).thenReturn(List.of(
                new CuentaRef(11L, 1L, d("100"), d(deuda1), "PENDIENTE"),
                new CuentaRef(12L, 1L, d("100"), d(deuda2), "PENDIENTE")));
        when(repo.siguienteNumeroAbono()).thenReturn("AB-00000001");
        when(repo.insertarAbono(any(), anyLong(), anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(99L);
        when(repo.buscarAbono(99L)).thenReturn(Optional.of(new AbonoResumen(99L, "AB-00000001", 1L, "Ana", 3L, "cajero",
                "EFECTIVO", d("120"), null, null, List.of())));
    }

    @Test
    void elAbonoSeAplicaALasDeudasMasAntiguasPrimero() {
        preparaAbono("100", "60");
        service.registrar(new RegistrarAbonoComando(1L, 1L, d("120"), null));

        verify(repo).insertarAplicacion(99L, 11L, d("100"));
        verify(repo).insertarAplicacion(99L, 12L, d("20.00"));
        verify(repo).actualizarCuenta(11L, d("0"), "PAGADA", "cajero");
        verify(repo).actualizarCuenta(eq(12L), eq(d("40.00")), eq("PENDIENTE"), eq("cajero"));
    }

    @Test
    void elAbonoNoPuedeSuperarLaDeuda() {
        preparaAbono("100", "60");
        assertThrows(AbonoExcedeDeudaException.class,
                () -> service.registrar(new RegistrarAbonoComando(1L, 1L, d("160.01"), null)));
    }

    @Test
    void abonarExigeTurnoAbierto() {
        when(repo.turnoAbiertoDeUsuario(7L)).thenReturn(Optional.empty());
        assertThrows(SinTurnoParaAbonoException.class,
                () -> service.registrar(new RegistrarAbonoComando(1L, 1L, d("10"), null)));
    }
}

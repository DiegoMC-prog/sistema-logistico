package com.tienda.backend.venta.application.service;

import com.tienda.backend.metodopago.domain.model.MetodoPago;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import com.tienda.backend.venta.application.model.AsignacionStock;
import com.tienda.backend.venta.application.model.ItemVentaComando;
import com.tienda.backend.venta.application.model.MetodoPagoRef;
import com.tienda.backend.venta.application.model.PagoVentaComando;
import com.tienda.backend.venta.application.model.PresentacionVenta;
import com.tienda.backend.venta.application.model.RegistrarVentaComando;
import com.tienda.backend.venta.application.model.TurnoVenta;
import com.tienda.backend.venta.application.port.out.CatalogoVentaPort;
import com.tienda.backend.venta.application.port.out.ClienteVentaPort;
import com.tienda.backend.venta.application.port.out.CreditoVentaPort;
import com.tienda.backend.venta.application.port.out.MetodoPagoVentaPort;
import com.tienda.backend.venta.application.port.out.NumeroVentaGeneradorPort;
import com.tienda.backend.venta.application.port.out.PrecioVentaPort;
import com.tienda.backend.venta.application.port.out.StockVentaPort;
import com.tienda.backend.venta.application.port.out.TurnoVentaPort;
import com.tienda.backend.venta.application.port.out.VentaDevolucionConsultaPort;
import com.tienda.backend.venta.application.port.out.VentaRepositoryPort;
import com.tienda.backend.venta.domain.exception.PagoInsuficienteException;
import com.tienda.backend.venta.domain.exception.SinTurnoAbiertoException;
import com.tienda.backend.venta.domain.exception.VentaDatosInvalidosException;
import com.tienda.backend.venta.domain.exception.VueltoSinEfectivoException;
import com.tienda.backend.venta.domain.model.PagoVenta;
import com.tienda.backend.venta.domain.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Función crítica 3: registro de una venta, foco en el cobro (pagos, vuelto y crédito). */
class VentaEjecutorPagosTest {

    private static final Long USUARIO = 7L;
    private static final Long TURNO = 3L;
    private static final Long PRESENTACION = 1L;
    private static final Long EFECTIVO = 1L;
    private static final Long TARJETA = 2L;
    private static final Long CREDITO = 3L;

    private VentaRepositoryPort ventaRepo;
    private TurnoVentaPort turnoPort;
    private StockVentaPort stockPort;
    private CreditoVentaPort creditoPort;
    private VentaEjecutor ejecutor;

    @BeforeEach
    void preparar() {
        ventaRepo = mock(VentaRepositoryPort.class);
        turnoPort = mock(TurnoVentaPort.class);
        stockPort = mock(StockVentaPort.class);
        creditoPort = mock(CreditoVentaPort.class);
        NumeroVentaGeneradorPort numeroPort = mock(NumeroVentaGeneradorPort.class);
        MetodoPagoVentaPort metodoPort = mock(MetodoPagoVentaPort.class);
        CatalogoVentaPort catalogoPort = mock(CatalogoVentaPort.class);
        PrecioVentaPort precioPort = mock(PrecioVentaPort.class);
        UsuarioActualPort usuario = mock(UsuarioActualPort.class);

        // Cajero con turno abierto en el depósito 1
        when(usuario.id()).thenReturn(USUARIO);
        TurnoVenta turno = new TurnoVenta(TURNO, 1L, 1L, true);
        when(turnoPort.turnoAbiertoDeUsuario(USUARIO)).thenReturn(Optional.of(turno));
        when(turnoPort.bloquearCompartido(TURNO)).thenReturn(Optional.of(turno));

        // Un producto de 100.00 con stock en un lote sin descuento
        when(catalogoPort.buscarPresentacion(PRESENTACION))
                .thenReturn(Optional.of(new PresentacionVenta(PRESENTACION, 10L, "Aceite", true, BigDecimal.ONE)));
        when(precioPort.precioAplicable(any(), any(), any())).thenReturn(d("100.00"));
        when(stockPort.salir(anyLong(), any(), any(), any()))
                .thenReturn(List.of(new AsignacionStock(0, 500L, d("1"), d("50.00"), d("0"))));

        // Devuelve solo los métodos pedidos, como el adaptador real
        Map<Long, MetodoPagoRef> catalogoMetodos = Map.of(
                EFECTIVO, new MetodoPagoRef(EFECTIVO, MetodoPago.NOMBRE_EFECTIVO, true),
                TARJETA, new MetodoPagoRef(TARJETA, "TARJETA", true),
                CREDITO, new MetodoPagoRef(CREDITO, MetodoPago.NOMBRE_CREDITO_CLIENTE, true));
        when(metodoPort.buscarPorIds(anyCollection())).thenAnswer(inv -> {
            java.util.Collection<Long> ids = inv.getArgument(0);
            return ids.stream().collect(java.util.stream.Collectors.toMap(id -> id, catalogoMetodos::get));
        });
        when(numeroPort.siguiente()).thenReturn("V-00000001");
        when(ventaRepo.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        ejecutor = new VentaEjecutor(ventaRepo, numeroPort, turnoPort, mock(ClienteVentaPort.class), metodoPort,
                catalogoPort, precioPort, stockPort, usuario, mock(VentaDevolucionConsultaPort.class), creditoPort,
                mock(com.tienda.backend.shared.application.port.out.AutorizacionOperacionPort.class));
    }

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    /** Venta de 1 unidad (total 100.00) sin cliente, con un único pago. */
    private static RegistrarVentaComando ventaCon(Long metodoPagoId, String monto) {
        return new RegistrarVentaComando(null,
                List.of(new ItemVentaComando(PRESENTACION, d("1"), null, null)),
                List.of(new PagoVentaComando(metodoPagoId, d(monto), null)), null);
    }

    @Test
    void pagoExactoEnEfectivoNoGeneraVuelto() {
        // Arrange
        RegistrarVentaComando cmd = ventaCon(EFECTIVO, "100.00");

        // Act
        Venta venta = ejecutor.registrar(cmd).venta();

        // Assert
        assertEquals(d("100.00"), venta.getTotal());
        assertEquals(d("0.00"), venta.getVuelto());
        assertEquals(List.of(new PagoVenta(null, EFECTIVO, d("100.00"), null)), venta.getPagos());
    }

    @Test
    void pagoDeMasEnEfectivoCalculaElVueltoYGuardaElNeto() {
        // Arrange
        RegistrarVentaComando cmd = ventaCon(EFECTIVO, "150.00");

        // Act
        Venta venta = ejecutor.registrar(cmd).venta();

        // Assert
        assertEquals(d("50.00"), venta.getVuelto());
        assertEquals(d("100.00"), venta.getPagos().get(0).monto());
    }

    @Test
    void pagoInsuficienteLanzaExcepcion() {
        // Arrange
        RegistrarVentaComando cmd = ventaCon(EFECTIVO, "80.00");

        // Act
        PagoInsuficienteException ex = assertThrows(PagoInsuficienteException.class, () -> ejecutor.registrar(cmd));

        // Assert: el mensaje indica cuánto falta
        assertTrue(ex.getMessage().contains("20"));
    }

    @Test
    void conTarjetaNoSePuedeDarVuelto() {
        // Arrange
        RegistrarVentaComando cmd = ventaCon(TARJETA, "150.00");

        // Act + Assert
        assertThrows(VueltoSinEfectivoException.class, () -> ejecutor.registrar(cmd));
    }

    @Test
    void ventaACreditoSinClienteSeRechazaAntesDeTocarElStock() {
        // Arrange
        RegistrarVentaComando cmd = ventaCon(CREDITO, "100.00");

        // Act + Assert
        assertThrows(VentaDatosInvalidosException.class, () -> ejecutor.registrar(cmd));
        verify(stockPort, never()).salir(anyLong(), any(), any(), any());
        verify(creditoPort, never()).crearCuenta(any(), any(), any());
    }

    @Test
    void sinTurnoAbiertoNoSePuedeVender() {
        // Arrange
        when(turnoPort.turnoAbiertoDeUsuario(USUARIO)).thenReturn(Optional.empty());
        RegistrarVentaComando cmd = ventaCon(EFECTIVO, "100.00");

        // Act + Assert
        assertThrows(SinTurnoAbiertoException.class, () -> ejecutor.registrar(cmd));
        verify(ventaRepo, never()).save(any());
    }
}

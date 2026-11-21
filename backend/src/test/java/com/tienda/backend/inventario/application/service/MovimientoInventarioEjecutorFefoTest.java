package com.tienda.backend.inventario.application.service;

import com.tienda.backend.deposito.domain.model.Deposito;
import com.tienda.backend.deposito.domain.model.EstadoDeposito;
import com.tienda.backend.inventario.application.model.AsignacionFefo;
import com.tienda.backend.inventario.application.model.ClaveStock;
import com.tienda.backend.inventario.application.model.ItemFefoComando;
import com.tienda.backend.inventario.application.model.LoteDisponible;
import com.tienda.backend.inventario.application.model.SalidaFefoComando;
import com.tienda.backend.inventario.application.port.out.DepositoConsultaPort;
import com.tienda.backend.inventario.application.port.out.InventarioRepositoryPort;
import com.tienda.backend.inventario.application.port.out.LoteConsultaPort;
import com.tienda.backend.inventario.application.port.out.LoteGestionPort;
import com.tienda.backend.inventario.application.port.out.MovimientoRepositoryPort;
import com.tienda.backend.inventario.application.port.out.PresentacionConsultaPort;
import com.tienda.backend.inventario.application.port.out.ProductoConsultaPort;
import com.tienda.backend.inventario.domain.exception.ProductoInactivoException;
import com.tienda.backend.inventario.domain.exception.StockInsuficienteProductoException;
import com.tienda.backend.inventario.domain.model.Inventario;
import com.tienda.backend.inventario.domain.model.Movimiento;
import com.tienda.backend.producto.domain.model.EstadoProducto;
import com.tienda.backend.producto.domain.model.Producto;
import com.tienda.backend.shared.application.port.out.UsuarioActualPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Función crítica 1: salida de stock FEFO (vence primero, sale primero). */
class MovimientoInventarioEjecutorFefoTest {

    private static final Long DEPOSITO = 1L;
    private static final Long PRODUCTO = 10L;
    private static final Long LOTE_A = 100L;   // vence antes
    private static final Long LOTE_B = 200L;   // vence después

    private InventarioRepositoryPort inventarioRepo;
    private MovimientoRepositoryPort movimientoRepo;
    private ProductoConsultaPort productoPort;
    private MovimientoInventarioEjecutor ejecutor;

    private Inventario stockA;
    private Inventario stockB;

    @BeforeEach
    void preparar() {
        inventarioRepo = mock(InventarioRepositoryPort.class);
        movimientoRepo = mock(MovimientoRepositoryPort.class);
        productoPort = mock(ProductoConsultaPort.class);
        DepositoConsultaPort depositoPort = mock(DepositoConsultaPort.class);
        UsuarioActualPort usuario = mock(UsuarioActualPort.class);

        when(usuario.id()).thenReturn(7L);
        when(depositoPort.obtenerPorId(DEPOSITO))
                .thenReturn(Optional.of(new Deposito(DEPOSITO, "Principal", null, EstadoDeposito.ACTIVO)));
        when(movimientoRepo.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));

        ejecutor = new MovimientoInventarioEjecutor(inventarioRepo, movimientoRepo, depositoPort, productoPort,
                mock(LoteConsultaPort.class), mock(LoteGestionPort.class), mock(PresentacionConsultaPort.class), usuario);
    }

    private static BigDecimal d(String v) {
        return new BigDecimal(v);
    }

    /** Producto activo con dos lotes: A (5 u., vence en 10 días) y B (10 u., vence en 60 días), ya en orden FEFO. */
    private void escenarioDosLotes(EstadoProducto estado) {
        when(productoPort.obtenerPorId(PRODUCTO))
                .thenReturn(Optional.of(new Producto(PRODUCTO, "Leche", null, 1L, d("0"), true, estado)));
        when(inventarioRepo.findLotesDisponiblesFefo(eq(DEPOSITO), anyCollection(), any(LocalDate.class))).thenReturn(List.of(
                new LoteDisponible(LOTE_A, PRODUCTO, LocalDate.now().plusDays(10), d("2.00"), d("0")),
                new LoteDisponible(LOTE_B, PRODUCTO, LocalDate.now().plusDays(60), d("2.50"), d("0"))));
        stockA = new Inventario(1L, DEPOSITO, LOTE_A, d("5"), 0L);
        stockB = new Inventario(2L, DEPOSITO, LOTE_B, d("10"), 0L);
        Map<ClaveStock, Inventario> bloqueados = new HashMap<>();
        bloqueados.put(new ClaveStock(DEPOSITO, LOTE_A), stockA);
        bloqueados.put(new ClaveStock(DEPOSITO, LOTE_B), stockB);
        when(inventarioRepo.bloquear(anyCollection())).thenReturn(bloqueados);
    }

    private static SalidaFefoComando salida(String... cantidades) {
        List<ItemFefoComando> items = java.util.Arrays.stream(cantidades)
                .map(c -> new ItemFefoComando(PRODUCTO, null, new BigDecimal(c))).toList();
        return new SalidaFefoComando(DEPOSITO, "Venta V-1", items, "VENTA", 1L);
    }

    @Test
    void saleDelLoteQueVencePrimero() {
        // Arrange
        escenarioDosLotes(EstadoProducto.ACTIVO);

        // Act
        MovimientoInventarioEjecutor.FefoEjecutado r = ejecutor.ejecutarSalidaFefo(salida("3"));

        // Assert
        assertEquals(List.of(new AsignacionFefo(0, LOTE_A, d("3"), d("2.00"), d("0"))), r.asignaciones());
        assertEquals(d("2"), stockA.getStockActual());
        assertEquals(d("10"), stockB.getStockActual());
        verify(inventarioRepo).save(stockA);
        verify(inventarioRepo, never()).save(stockB);
    }

    @Test
    void reparteEntreLotesCuandoUnoNoAlcanza() {
        // Arrange
        escenarioDosLotes(EstadoProducto.ACTIVO);

        // Act
        MovimientoInventarioEjecutor.FefoEjecutado r = ejecutor.ejecutarSalidaFefo(salida("8"));

        // Assert
        assertEquals(2, r.asignaciones().size());
        assertEquals(LOTE_A, r.asignaciones().get(0).loteId());
        assertEquals(d("5"), r.asignaciones().get(0).cantidadBase());
        assertEquals(LOTE_B, r.asignaciones().get(1).loteId());
        assertEquals(d("3"), r.asignaciones().get(1).cantidadBase());
        assertEquals(0, stockA.getStockActual().signum());
        assertEquals(d("7"), stockB.getStockActual());
    }

    @Test
    void stockInsuficienteLanzaExcepcionYNoRegistraMovimiento() {
        // Arrange
        escenarioDosLotes(EstadoProducto.ACTIVO);

        // Act + Assert
        assertThrows(StockInsuficienteProductoException.class, () -> ejecutor.ejecutarSalidaFefo(salida("20")));
        verify(movimientoRepo, never()).save(any());
        verify(inventarioRepo, never()).save(any());
    }

    @Test
    void dosLineasDelMismoProductoNoUsanElMismoStock() {
        // Arrange
        escenarioDosLotes(EstadoProducto.ACTIVO);

        // Act
        MovimientoInventarioEjecutor.FefoEjecutado r = ejecutor.ejecutarSalidaFefo(salida("4", "4"));

        // Assert: la 1.ª línea toma 4 de A; la 2.ª toma el 1 que queda en A y 3 de B
        assertEquals(List.of(
                new AsignacionFefo(0, LOTE_A, d("4"), d("2.00"), d("0")),
                new AsignacionFefo(1, LOTE_A, d("1"), d("2.00"), d("0")),
                new AsignacionFefo(1, LOTE_B, d("3"), d("2.50"), d("0"))), r.asignaciones());
        assertEquals(0, stockA.getStockActual().signum());
        assertEquals(d("7"), stockB.getStockActual());
    }

    @Test
    void productoInactivoSeRechaza() {
        // Arrange
        escenarioDosLotes(EstadoProducto.INACTIVO);

        // Act + Assert
        assertThrows(ProductoInactivoException.class, () -> ejecutor.ejecutarSalidaFefo(salida("1")));
        verify(inventarioRepo, never()).bloquear(anyCollection());
    }
}

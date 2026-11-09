package com.tienda.backend.pedido.domain.model;

import com.tienda.backend.pedido.domain.exception.PedidoDatosInvalidosException;
import com.tienda.backend.pedido.domain.exception.PedidoEstadoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoProveedorTest {

    private static BigDecimal bd(String valor) {
        return new BigDecimal(valor);
    }

    /** Pedido ya guardado: las líneas tienen id (10 y 11) para poder recibirlas. */
    private PedidoProveedor pedido(EstadoPedido estado) {
        return new PedidoProveedor(1L, "PP-00000001", 5L, 7L, estado, LocalDate.now(), null, null, 0L, List.of(
                new LineaPedido(10L, 100L, bd("10"), BigDecimal.ZERO, bd("2.5")),
                new LineaPedido(11L, 200L, bd("4"), BigDecimal.ZERO, bd("1"))));
    }

    @Test
    void elPedidoNuevoNaceEnBorradorYCalculaElTotal() {
        PedidoProveedor p = PedidoProveedor.nuevo("PP-1", 5L, 7L, null, "  urgente ", List.of(
                new LineaPedido(100L, bd("10"), bd("2.5")), new LineaPedido(200L, bd("4"), bd("1"))));

        assertEquals(EstadoPedido.BORRADOR, p.getEstado());
        assertEquals(0, bd("29").compareTo(p.getTotal()));
        assertEquals("urgente", p.getObservacion());
    }

    @Test
    void noPermiteProductosRepetidos() {
        assertThrows(PedidoDatosInvalidosException.class, () -> PedidoProveedor.nuevo("PP-1", 5L, 7L, null, null, List.of(
                new LineaPedido(100L, bd("1"), bd("1")), new LineaPedido(100L, bd("2"), bd("1")))));
    }

    @Test
    void laFechaEstimadaNoPuedeSerAnteriorAlPedido() {
        assertThrows(PedidoDatosInvalidosException.class, () -> PedidoProveedor.nuevo("PP-1", 5L, 7L,
                LocalDate.now().minusDays(1), null, List.of()));
    }

    @Test
    void noSePuedeEnviarSinLineas() {
        PedidoProveedor p = PedidoProveedor.nuevo("PP-1", 5L, 7L, null, null, List.of());
        assertThrows(PedidoDatosInvalidosException.class, p::enviar);
    }

    @Test
    void soloUnBorradorSeEdita() {
        PedidoProveedor enviado = pedido(EstadoPedido.ENVIADO);
        assertThrows(PedidoEstadoInvalidoException.class, () -> enviado.editar(5L, 7L, null, null, List.of()));
    }

    @Test
    void laRecepcionParcialYLaTotalCambianElEstado() {
        PedidoProveedor p = pedido(EstadoPedido.ENVIADO);

        p.recibir(10L, bd("4"));
        assertEquals(EstadoPedido.RECIBIDO_PARCIAL, p.getEstado());
        assertEquals(0, bd("6").compareTo(p.linea(10L).pendiente()));

        p.recibir(10L, bd("6"));
        assertEquals(EstadoPedido.RECIBIDO_PARCIAL, p.getEstado());   // falta la otra línea

        p.recibir(11L, bd("4"));
        assertEquals(EstadoPedido.RECIBIDO, p.getEstado());
        assertTrue(p.getLineas().stream().allMatch(LineaPedido::completa));
    }

    @Test
    void noSePuedeRecibirMasDeLoPendiente() {
        PedidoProveedor p = pedido(EstadoPedido.ENVIADO);
        assertThrows(PedidoDatosInvalidosException.class, () -> p.recibir(10L, bd("10.0001")));
        assertThrows(PedidoDatosInvalidosException.class, () -> p.recibir(10L, BigDecimal.ZERO));
    }

    @Test
    void noSeRecibeUnaLineaQueNoEsDelPedido() {
        PedidoProveedor p = pedido(EstadoPedido.ENVIADO);
        assertThrows(PedidoDatosInvalidosException.class, () -> p.recibir(99L, bd("1")));
    }

    @Test
    void noSeRecibeUnPedidoQueNoFueEnviadoOYaEstaCerrado() {
        assertThrows(PedidoEstadoInvalidoException.class, () -> pedido(EstadoPedido.BORRADOR).recibir(10L, bd("1")));
        assertThrows(PedidoEstadoInvalidoException.class, () -> pedido(EstadoPedido.CANCELADO).recibir(10L, bd("1")));
        assertThrows(PedidoEstadoInvalidoException.class, () -> pedido(EstadoPedido.RECIBIDO).recibir(10L, bd("1")));
    }

    @Test
    void soloSeCancelaUnPedidoSinRecepciones() {
        pedido(EstadoPedido.BORRADOR).cancelar();
        pedido(EstadoPedido.ENVIADO).cancelar();
        assertThrows(PedidoEstadoInvalidoException.class, () -> pedido(EstadoPedido.RECIBIDO_PARCIAL).cancelar());
        assertThrows(PedidoEstadoInvalidoException.class, () -> pedido(EstadoPedido.RECIBIDO).cancelar());
    }
}
